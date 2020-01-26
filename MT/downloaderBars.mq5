//+------------------------------------------------------------------+
//|                                        RSI_MA_Downloader.ex5.mq5 |
//|                        Copyright 2019, MetaQuotes Software Corp. |
//|                                             https://www.mql5.com |
//+------------------------------------------------------------------+

#property version   "1.00"

#include <MQLMySQL.mqh>

input int      Period= 12;
input int      SL= 30;
input int      TP= 50;
input int      Magic= 1;
input double   Lot = 0.1;

int rsiHandle; // handle for our ADX indicator
int maHandle;  // handle for our Moving Average indicator
int volHandle; // handle for volume indicator
int forceHandle; // handle for forxe index indicator
int acHandle; // handle for Acceletator oscilator indicator
int sarHandle;
int momentumHandle;



double rsiVal[];// Dynamic array to hold the values of RSI for each bars
double maVal[]; // Dynamic array to hold the values of Moving Average for each bars
double volVal[]; 
double forceVal[]; 
double acVal[]; 
double sarVal[];
double momentumVal[];

double p_close; // Variable to store the close value of a bar
int STP, TKP;   // To be used for Stop Loss & Take Profit values
int DB; // database identifier

//+------------------------------------------------------------------+
//| Expert initialization function                                   |
//+------------------------------------------------------------------+



int OnInit()
  {

//--- Get handle for RSI indicator
   rsiHandle=iRSI(_Symbol,_Period,Period,PRICE_CLOSE);
   maHandle=iMA(_Symbol,_Period,Period,0,MODE_EMA,PRICE_CLOSE);
   volHandle= iVolumes(_Symbol,_Period,VOLUME_REAL);
   forceHandle=iForce(_Symbol,_Period,Period,MODE_SMA,VOLUME_REAL);
   acHandle=iAC(_Symbol,_Period);
   sarHandle=iSAR(_Symbol,_Period,0.02,0.2);
   momentumHandle = iMomentum(_Symbol,_Period,Period,PRICE_CLOSE);

//--- What if handle returns Invalid Handle
   if(rsiHandle<0 || maHandle<0 || volHandle <0 || forceHandle <0 || acHandle < 0 || sarHandle < 0 || momentumHandle < 0)
     {
      Alert("Error Creating Handles for indicators - error: ",GetLastError(),"!!");
     }
   
   string Host, UserForDB, Password, Database, Socket; // database credentials
   int Port,ClientFlag;
   
   Host = "ls-7747fafb702e9b0e95827d986e35040c609dd263.cztwonmsggwh.eu-west-2.rds.amazonaws.com";
   UserForDB = "mehowmeta2";
   Password = "AReallyStrongPass";
   Database = "bardata";
   Port     = 3306;
   Socket   = "0";
   ClientFlag = 8;// What is a client flag? pls google.
   
   Alert ("Host: ",Host, ", User: ", UserForDB, ", Database: ",Database);
 
   // open database connection
   Alert ("Connecting...");
   
   DB = MySqlConnect(Host, UserForDB, Password, Database, Port, Socket, ClientFlag);
   
   if (DB == -1) { Alert("Connection failed! Error: "+MySqlErrorDescription); return INIT_FAILED; } else { Alert ("Connected! DBID#",DB);}
   
   return(INIT_SUCCEEDED);
  }
//+------------------------------------------------------------------+
//| Expert deinitialization function                                 |
//+------------------------------------------------------------------+
void OnDeinit(const int reason)
  {
   //--- Releasing indicators

   IndicatorRelease(rsiHandle);
   IndicatorRelease(maHandle);
   IndicatorRelease(volHandle);
   IndicatorRelease(forceHandle);
   IndicatorRelease(acHandle);
   IndicatorRelease(sarHandle);
   IndicatorRelease(momentumHandle);
   
   MySqlDisconnect(DB);
  }
//+------------------------------------------------------------------+
//| Expert tick function                                             |
//+------------------------------------------------------------------+
void OnTick()
   {
  
   // Do we have enough bars to work with
   if(Bars(_Symbol,_Period)<500) // if total bars is less than 60 bars
      {
      Alert("We have less than 500 bars, EA will now exit!!");
      return;
      }
   // We will use the static Old_Time variable to serve the bar time.   
   // At each OnTick execution we will check the current bar time with the saved one.
   // If the bar time isn't equal to the saved time, it indicates that we have a new tick.
   static datetime Old_Time;
   datetime New_Time[1];
   bool IsNewBar=false;
   
   // copying the last bar time to the element New_Time[0]
   int copied=CopyTime(_Symbol,_Period,0,1,New_Time);
   if(copied>0) // ok, the data has been copied successfully
     {
      if(Old_Time!=New_Time[0]) // if old time isn't equal to new bar time
        {
         IsNewBar=true;   // if it isn't a first call, the new bar has appeared
         if(MQL5InfoInteger(MQL5_DEBUGGING)) Print("We have new bar here ",New_Time[0]," old time was ",Old_Time);
         Old_Time=New_Time[0];            // saving bar time
        }
     }
   else
     {
      Alert("Error in copying historical times data, error =",GetLastError());
      ResetLastError();
      return;
     }

   //--- EA should only check for new trade if we have a new bar
   if(IsNewBar==false)
     {
      return;
     }
 
   //--- Do we have enough bars to work with
   int Mybars=Bars(_Symbol,_Period);
   if(Mybars<500) // if total bars is less than 60 bars
     {
      Alert("We have less than 500 bars, EA will now exit!!");
      return;
     }

   //--- Define some MQL5 Structures we will use for our trade
   MqlTick latest_price;     // To be used for getting recent/latest price quotes
   MqlTradeRequest mrequest;  // To be used for sending our trade requests
   MqlTradeResult mresult;    // To be used to get our trade results
   MqlRates mrate[];         // To be used to store the prices, volumes and spread of each bar
   ZeroMemory(mrequest);     // Initialization of mrequest structure
   //---
   
   /*
     Let's make sure our arrays values for the Rates, ADX Values and MA values 
     is store serially similar to the timeseries array
   */
   // the rates arrays
   ArraySetAsSeries(mrate,true);
   ArraySetAsSeries(rsiVal,true);
   ArraySetAsSeries(maVal,true);
   ArraySetAsSeries(volVal,true);
   ArraySetAsSeries(forceVal,true);
   ArraySetAsSeries(acVal,true);
   ArraySetAsSeries(sarVal,true);
   ArraySetAsSeries(momentumVal,true);
   
   //--- Get the last price quote using the MQL5 MqlTick Structure
   if(!SymbolInfoTick(_Symbol,latest_price))
     {
      Alert("Error getting the latest price quote - error:",GetLastError(),"!!");
      return;
     }
     
   //--- Get the details of the latest 3 bars
   if(CopyRates(_Symbol,_Period,0,5,mrate)<0)
     {
      Alert("Error copying rates/history data - error:",GetLastError(),"!!");
      return;
     }
     
   //--- Copy the new values of our indicators to buffers (arrays) using the handle
  
   if(CopyBuffer(maHandle,0,0,5,maVal)<0)
     {
      Alert("Error copying Moving Average indicator buffer - error:",GetLastError());
      return;
     }
   if(CopyBuffer(rsiHandle,0,0,5,rsiVal)<0)
     {
      Alert("Error copying RSI indicator buffer - error:",GetLastError());
      return;
     }
   //if(CopyBuffer(volHandle,0,0,5,volVal)<0)
   //{
   //Alert("Error copying Volume indicator buffer - error:",GetLastError());
   //return;
   //}
   //if(CopyBuffer(forceHandle,0,0,5,forceVal)<0)
   //{
   //Alert("Error copying Force indicator buffer - error:",GetLastError());
   //return;
   //}
   if(CopyBuffer(acHandle,0,0,5,acVal)<0)
     {
      Alert("Error copying AC indicator buffer - error:",GetLastError());
      return;
     }
      
   if(CopyBuffer(sarHandle,0,0,5,sarVal)<0)
     {
      Alert("Error copying SAR indicator buffer - error:",GetLastError());
      return;
     }
   if(CopyBuffer(momentumHandle,0,0,5,momentumVal)<0)
     {
      Alert("Error copying Momentum indicator buffer - error:",GetLastError());
      return;
     }
      
   double price = mrate[0].close;
   double rsi = rsiVal[0];
   double ma = maVal[0];
   double ac = acVal[0];
   double sar = sarVal[0];
   double momentum = momentumVal[0];
   
   //jacks first COMMIT
   
   
   string query = "";
   query = ("INSERT INTO bars (timeofprice, closeprice, rsi, movingaverage, ac, sar, momentum) VALUES ('" + TimeCurrent() + "','" + price + "','" + rsi + "','" + ma + "','" + ac + "','" + sar + "','" + momentum + "');");
   MySqlExecute(DB, query);
  
   string subfolder="Research";
   string line = "";
   int filehandle=FileOpen(subfolder+"\\dataFile.txt",FILE_WRITE|FILE_READ|FILE_TXT);
   if(filehandle!=INVALID_HANDLE)
     {  
      line = TimeCurrent() + "," + price + "," + rsi + "," + ma + "," + ac + "," + sar + "," + momentum;  
      FileSeek(filehandle,0,SEEK_END);
      FileWrite(filehandle,line);
      FileFlush(filehandle);
      FileClose(filehandle);
     }
   else 
     {
      Print("File open failed, error ",GetLastError()); 
      return;
     }
  }
//+------------------------------------------------------------------+

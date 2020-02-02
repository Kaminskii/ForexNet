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

int rsiHandle;       double rsiVal[];// Inidcator handles
int maHandle;        double maVal[];
int volHandle;       double volVal[];
int forceHandle;     double forceVal[];
int acHandle;        double acVal[];
int sarHandle;       double sarVal[];
int momentumHandle;  double momentumVal[];
int adHandle;        double adVal[];
int amaHandle;       double amaVal[];
int aoHandle;        double aoVal[];
int atrHandle;       double atrVal[];
int bearsHandle;     double bearsVal[];
int bullsHandle;     double bullsVal[];
int cciHandle;       double cciVal[];
int chaikinHandle;   double chaikinVal[];
int demaHandle;      double demaVal[];
int demarkerHandle;  double demarkerVal[];
int framaHandle;     double framaVal[];
int bwmfiHandle;     double bwmfiVal[];
int osmaHandle;      double osmaVal[];
int obvHandle;       double obvVal[];
int temaHandle;      double temaVal[];
int trixHandle;      double trixVal[];
int wprHandle;       double wprVal[];
int vidyaHandle;     double vidyaVal[];


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
   volHandle= iVolumes(_Symbol,_Period,VOLUME_TICK);
   forceHandle=iForce(_Symbol,_Period,Period,MODE_SMA,VOLUME_TICK);
   acHandle=iAC(_Symbol,_Period);
   sarHandle=iSAR(_Symbol,_Period,0.02,0.2);
   momentumHandle = iMomentum(_Symbol,_Period,Period,PRICE_CLOSE);
   adHandle = iAD(_Symbol,_Period,VOLUME_TICK);
   amaHandle =iAMA(_Symbol,_Period,Period,Period - 2,Period + 2,2,PRICE_CLOSE);
   aoHandle = iAO(_Symbol,_Period);
   atrHandle = iATR(_Symbol,_Period,Period);
   bearsHandle = iBearsPower(_Symbol,_Period,Period);
   bullsHandle = iBullsPower(_Symbol,_Period,Period);
   cciHandle = iCCI(_Symbol,_Period,Period,PRICE_CLOSE);
   chaikinHandle = iChaikin(_Symbol,_Period,Period - 2,Period + 2,MODE_SMMA,VOLUME_TICK);
   demaHandle = iDEMA(_Symbol,_Period,Period,2,PRICE_CLOSE);
   demarkerHandle = iDeMarker(_Symbol,_Period,Period);
   framaHandle = iFrAMA(_Symbol,_Period,Period,2,PRICE_CLOSE);
   bwmfiHandle = iBWMFI(_Symbol,_Period,VOLUME_TICK);
   osmaHandle = iOsMA(_Symbol,_Period,Period - 2,Period + 2,2,PRICE_CLOSE);
   obvHandle = iOBV(_Symbol,_Period,VOLUME_TICK);
   temaHandle = iTEMA(_Symbol,_Period,Period,2,PRICE_CLOSE);
   trixHandle = iTriX(_Symbol,_Period,Period,PRICE_CLOSE);
   wprHandle = iWPR(_Symbol,_Period,Period);
   vidyaHandle = iVIDyA(_Symbol,_Period,Period,Period,2,PRICE_CLOSE);
   
   
   
//--- What if handle returns Invalid Handle
   if(rsiHandle<0 || maHandle<0 || volHandle <0 || forceHandle <0 || acHandle < 0 || sarHandle < 0 || momentumHandle < 0 || adHandle < 0 || amaHandle < 0
    || aoHandle < 0  || atrHandle < 0  || bearsHandle < 0  || bullsHandle < 0  || cciHandle < 0  || chaikinHandle < 0 || demaHandle < 0  || demarkerHandle < 0
    || framaHandle < 0  || bwmfiHandle < 0  || osmaHandle < 0  || obvHandle < 0  || temaHandle < 0  || trixHandle < 0  || wprHandle < 0  || vidyaHandle < 0)
     {
      Alert("Error Creating Handles for indicators - error: ",GetLastError(),"!!");
     }
   
   string Host, UserForDB, Password, Database, Socket; // database credentials
   int Port,ClientFlag;
   
   Host = "ls-7747fafb702e9b0e95827d986e35040c609dd263.cztwonmsggwh.eu-west-2.rds.amazonaws.com";
   UserForDB = "mehowmeta2";
   Password = "Once there was a spliff";
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
   IndicatorRelease(adHandle);
   IndicatorRelease(amaHandle);
   IndicatorRelease(aoHandle);
   IndicatorRelease(atrHandle);
   IndicatorRelease(bearsHandle);
   IndicatorRelease(bullsHandle);
   IndicatorRelease(cciHandle);
   IndicatorRelease(chaikinHandle);
   IndicatorRelease(demaHandle);
   IndicatorRelease(demarkerHandle);
   IndicatorRelease(framaHandle);
   IndicatorRelease(bwmfiHandle);
   IndicatorRelease(osmaHandle);
   IndicatorRelease(obvHandle);
   IndicatorRelease(temaHandle);
   IndicatorRelease(trixHandle);
   IndicatorRelease(wprHandle);
   IndicatorRelease(vidyaHandle);
   
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
   ArraySetAsSeries(mrate,true); ArraySetAsSeries(aoVal,true); ArraySetAsSeries(demaVal,true);   ArraySetAsSeries(wprVal,true);   
   ArraySetAsSeries(rsiVal,true); ArraySetAsSeries(amaVal,true); ArraySetAsSeries(demarkerVal,true); ArraySetAsSeries(vidyaVal,true);     
   ArraySetAsSeries(maVal,true); ArraySetAsSeries(atrVal,true);   ArraySetAsSeries(framaVal,true);   
   ArraySetAsSeries(volVal,true); ArraySetAsSeries(adVal,true);   ArraySetAsSeries(bwmfiVal,true);   
   ArraySetAsSeries(forceVal,true); ArraySetAsSeries(bearsVal,true); ArraySetAsSeries(osmaVal,true);   
   ArraySetAsSeries(acVal,true); ArraySetAsSeries(bullsVal,true);   ArraySetAsSeries(obvVal,true);   
   ArraySetAsSeries(sarVal,true); ArraySetAsSeries(cciVal,true);   ArraySetAsSeries(temaVal,true);   
   ArraySetAsSeries(momentumVal,true); ArraySetAsSeries(chaikinVal,true); ArraySetAsSeries(trixVal,true);     
   
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
 
   if(CopyBuffer(maHandle,0,0,5,maVal)<0){Alert("Error copying Moving Average indicator buffer - error:",GetLastError());return;}
   if(CopyBuffer(rsiHandle,0,0,5,rsiVal)<0){Alert("Error copying RSI indicator buffer - error:",GetLastError());return;}
   if(CopyBuffer(acHandle,0,0,5,acVal)<0){Alert("Error copying AC indicator buffer - error:",GetLastError());return;}
   if(CopyBuffer(sarHandle,0,0,5,sarVal)<0){Alert("Error copying SAR indicator buffer - error:",GetLastError());return;}
   if(CopyBuffer(momentumHandle,0,0,5,momentumVal)<0){Alert("Error copying MOMENTUM indicator buffer - error:",GetLastError());return;}
   if(CopyBuffer(volHandle,0,0,5,volVal)<0){Alert("Error copying VOL indicator buffer - error:",GetLastError());return;}
   if(CopyBuffer(forceHandle,0,0,5,forceVal)<0){Alert("Error copying FORCE indicator buffer - error:",GetLastError());return;}
   if(CopyBuffer(adHandle,0,0,5,adVal)<0){Alert("Error copying AD indicator buffer - error:",GetLastError());return;}
   if(CopyBuffer(amaHandle,0,0,5,amaVal)<0){Alert("Error copying AMA indicator buffer - error:",GetLastError());return;}
   if(CopyBuffer(atrHandle,0,0,5,atrVal)<0){Alert("Error copying ATR indicator buffer - error:",GetLastError());return;}
   if(CopyBuffer(aoHandle,0,0,5,aoVal)<0){Alert("Error copying AO indicator buffer - error:",GetLastError());return;}
   if(CopyBuffer(bearsHandle,0,0,5,bearsVal)<0){Alert("Error copying BEARS indicator buffer - error:",GetLastError());return;}
   if(CopyBuffer(bullsHandle,0,0,5,bullsVal)<0){Alert("Error copying BULLS indicator buffer - error:",GetLastError());return;}
   if(CopyBuffer(cciHandle,0,0,5,cciVal)<0){Alert("Error copying CCI indicator buffer - error:",GetLastError());return;}
   if(CopyBuffer(chaikinHandle,0,0,5,chaikinVal)<0){Alert("Error copying CHAINKIN indicator buffer - error:",GetLastError());return;}
   if(CopyBuffer(demaHandle,0,0,5,demaVal)<0){Alert("Error copying DEMA indicator buffer - error:",GetLastError());return;}
   if(CopyBuffer(demarkerHandle,0,0,5,demarkerVal)<0){Alert("Error copying DEMARKER indicator buffer - error:",GetLastError());return;}
   if(CopyBuffer(framaHandle,0,0,5,framaVal)<0){Alert("Error copying FRAMA indicator buffer - error:",GetLastError());return;}
   if(CopyBuffer(bwmfiHandle,0,0,5,bwmfiVal)<0){Alert("Error copying BWMFI indicator buffer - error:",GetLastError());return;}
   if(CopyBuffer(osmaHandle,0,0,5,osmaVal)<0){Alert("Error copying OSMA indicator buffer - error:",GetLastError());return;}
   if(CopyBuffer(obvHandle,0,0,5,obvVal)<0){Alert("Error copying OBV indicator buffer - error:",GetLastError());return;}   
   if(CopyBuffer(temaHandle,0,0,5,temaVal)<0){Alert("Error copying TEMA indicator buffer - error:",GetLastError());return;}
   if(CopyBuffer(trixHandle,0,0,5,trixVal)<0){Alert("Error copying TRIX indicator buffer - error:",GetLastError());return;}
   if(CopyBuffer(wprHandle,0,0,5,wprVal)<0){Alert("Error copying WPR indicator buffer - error:",GetLastError());return;}
   if(CopyBuffer(vidyaHandle,0,0,5,vidyaVal)<0){Alert("Error copying VIDYA indicator buffer - error:",GetLastError());return;}
     
      
   double price = mrate[0].close;   double vol = volVal[0];    double bears = bearsVal[0];      double frama = framaVal[0]; double wpr = wprVal[0];
   double rsi = rsiVal[0];          double force = forceVal[0];double bulls = bullsVal[0];      double bwmfi = bwmfiVal[0]; double vidya = vidyaVal[0];          
   double ma = maVal[0];            double ad = adVal[0];      double cci = cciVal[0];          double osma = osmaVal[0];
   double ac = acVal[0];            double ama = amaVal[0];    double chaikin = chaikinVal[0];  double obv = obvVal[0];
   double sar = sarVal[0];          double atr = atrVal[0];    double dema = demaVal[0];        double tema = temaVal[0];
   double momentum = momentumVal[0];double ao = aoVal[0];      double demarker = demarkerVal[0];double trix = trixVal[0];
   
   //jacks first COMMIT
   
   
   string query = "";// ignore means you won't see errors when there are duplicates. However means my server errors yet. I don't like errors on my server -_-
   query = ("INSERT IGNORE INTO bardata2 (date, closePrice, rsi, ma, ac, sar, momentum, vol, forceIndex, ad, ama, atr, ao, bears, bulls, cci, chaikin, dema, demarker, frama, bwmfi, osma, obv, tema, trix, wpr, vidya) VALUES ('" 
   + TimeCurrent() + "','" + price + "','" + rsi + "','" + ma + "','" + ac + "','" + sar + "','" + momentum + "','" + VOLUME_TICK + "','" + force + "','" + ad + "','" + ama + "','" + atr + "','" + ao + "','" + bears + "','" + bulls + "','" + cci + "','" + chaikin + "','" + dema + "','" + demarker + "','" + frama + "','" + bwmfi + "','" + osma + "','" + obv + "','" + tema + "','" + trix + "','" + wpr + "','" +vidya +"');");
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

# investmentSimulator
Simulator of investing based on MACD vs investing a fixed amount daily. Interesting result!

## Usage:

### Generate MACD signals in result.txt
java MACDSignalDetector prices.txt result.txt

### Simulate Buying based on signals (assuming £1 per day) either accumulated for investment on next BUY signal vs invested daily irrespective of signal

java InvestmentSimulator result.txt 1 simulation.csv

### example for price retrieval (need ot pretty-rpint JSON and extract open/close prices):
https://query1.finance.yahoo.com/v8/finance/chart/MSFT?events=capitalGain%7Cdiv%7Csplit&formatted=true&includeAdjustedClose=true&interval=1d&period1=1122508800&period2=1753734052&symbol=MSFT&userYfid=true&lang=en-US&region=US

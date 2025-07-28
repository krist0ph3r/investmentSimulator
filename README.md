# investmentSimulator
Simulator of investing based on MACD vs investing a fixed amount daily. Interesting result!

## Usage:

### Generate MACD signals in result.txt
java MACDSignalDetector prices.txt result.txt

### Simulate Buying based on signals (assuming £1 per day) either accumulated for investment on next BUY signal vs invested daily irrespective of signal

java InvestmentSimulator result.txt 1 simulation.csv

package game;

import java.util.ArrayList;

public class ChessBoardAI
{
	ChessBoard gameBoard;
	boolean playingAs;
	
	double[] bestEight;
	
	//VALUE MULTIPLIERS
	int checkMult;
	int pawnValue;
	int rookValue;
	int knightValue;
	int bishValue;
	int queenValue;
	
	public ChessBoardAI(ChessBoard main, boolean playAs)
	{
		gameBoard = main;
		playingAs = playAs;
		
		bestEight = new double[8];
		
		checkMult = 10;
		pawnValue = 1;
		rookValue = 5;
		knightValue = 3;
		bishValue = 3;
		queenValue = 9;
		
		while(true)
		{
			//System.out.println("hello?");
			if(gameBoard.turn == playAs)
			{
				System.out.println("got here");
				makeBestMove();
			}
			try {Thread.sleep(0);} catch (InterruptedException e) {}
		}
	}
	
	public int value(ChessBoard board, boolean white)
	{
		int value = 0;
		
		//checkmate is very bad
		if(white && board.whiteCheckmate()) {return 0;}
		if(!white && board.blackCheckmate()) {return 0;}
		
		//check is bad also
		if(white && board.whiteCheck()) {value -= checkMult;}
		if(!white && board.blackCheck()) {value -= checkMult;}
		
		//adds the value of the peices on the board
		for(int x = 0; x < 8; x++)
		{
			for(int y = 0; y < 8; y++)
			{
				//System.out.println("evaluating properly");
				if(white)
				{
					int p;
					if((p = board.getBoardPieces()[x][y]) > 0)
					{
						switch(p)
						{
						case 1: value += pawnValue; break;
						case 2: value += rookValue; break;
						case 3: value += knightValue; break;
						case 4: value += bishValue; break;
						case 5: value += queenValue; break;
						default: break;
						}
					}
				}
				else
				{
					int p;
					if((p = board.getBoardPieces()[x][y]) < 0)
					{
						switch(p)
						{
						case -1: value += pawnValue; break;
						case -2: value += rookValue; break;
						case -3: value += knightValue; break;
						case -4: value += bishValue; break;
						case -5: value += queenValue; break;
						default: break;
						}
					}
				}
			}
		}
		
		//oh boy...
		
		//System.out.println("value is: " + value);
		
		
		
		
		return value;
	}
	
	public void makeBestMove()
	{
		makeMove(gameBoard, evaluateBestMove());
	}
	
	public double evaluateBestMove()
	{
		int topValue = 0;
		int posBestMove = 0;
		
		double[] top8 = new double[8];
		getBestEight(gameBoard, playingAs);
		for(int load = 0; load<8; load++)
		{
			top8[load] = bestEight[load];
			//System.out.println(bestEight[load]);
		}
		ChessBoard tester = new ChessBoard(gameBoard);
		for(int stepTopMoves = 0; stepTopMoves < 8; stepTopMoves++)
		{
			int holdBestValue;
			//System.out.println("calm before the storm");
			makeMove(tester, top8[stepTopMoves]);
			//tester.printBoard();
			if((holdBestValue = getFourthLayerBestValue(tester, playingAs)) > topValue)
			{
				//System.out.println("woo");
				topValue = holdBestValue;
				posBestMove = stepTopMoves;
			}
			tester.setBoardValuesEqual(gameBoard);
		}
		
		return top8[posBestMove];
	}
	
	public void getBestEight(ChessBoard board, boolean white)
	{
		//System.out.println("knock knoc");
		ChessBoard hold = new ChessBoard(board);

		double[] moves = new double[8];
		int[] values = new int[8];
		
		for(int load = 0; load < 8; load++)
		{
			moves[load] = 0;
			values[load] = 0;
		}
		
		int holdValue;
		double holdMove;
		
		for(int x = 0; x < 8; x++)
		{
			for(int y = 0; y < 8; y++)
			{
			//	System.out.println("("+x+", "+y+")");
				if((white && hold.getBoardPieces()[x][y] > 0) || (!white && hold.getBoardPieces()[x][y] < 0))
				{
					//System.out.println("should be here");
					int indexMaxMoves = -1;
					int[] legals = new int[27];
					hold.getLegalTiles(x, y);
					//System.out.println(legals[0]);
					//System.out.println(legals[1]);
					//System.out.println(legals[2]);
					
					getMaxMoves: for(int step = 0; step < 27; step++)
					{
						legals[step] = hold.legals[step];
						if(legals[step] != 0)
						{
							indexMaxMoves++;
						}
						else
						{
							break getMaxMoves;
						}
					}
					
					for(int step = 0; step <= indexMaxMoves; step++)
					{
						int xf = 0;
						int yf = 0;
					//	System.out.println(indexMaxMoves + " "+ step);
						
						int nextLegal = legals[step];
						//System.out.println(nextLegal);
						while(nextLegal%3==0){nextLegal/=3;xf++;}
						while(nextLegal%5==0){nextLegal/=5;yf++;}
						//System.out.println("got: " + xf + " " + yf);
						hold.moveAI(x, y, xf, yf);
						//hold.printBoard();
						holdValue = value(hold,white);
						//System.out.println("hold avlue is: "+ holdValue);
						holdMove = move2Double(x,y,xf,yf);
						//System.out.println("tjs move is "+ holdMove);
						insertData: for(int loadStep = 0; loadStep<8; loadStep++)
						{
							if(holdValue > values[loadStep])
							{
								for(int backStep = 7; backStep >= loadStep; backStep--)
								{
									if(backStep != 0) //makes sure we don't try to pull the value from index -1 into slot 1
									{
										values[backStep] = values[backStep-1];
										moves[backStep] = moves[backStep-1];
									}
									//System.out.println("getting here_________");
									
								}
								values[loadStep] = holdValue;
								moves[loadStep] = holdMove;
								break insertData;
							}
						}
						//hold.printBoard();System.out.println("done pring");
						hold.setBoardValuesEqual(board);//hold.printBoard();
					}//System.out.println("and then out");
				}//System.out.println("and then out again");
			}
		}
		//System.out.println("out of hte loop");
		for(int load = 0; load < 8; load++)
		{
			bestEight[load] = moves[load];
			//System.out.println("ths: " + moves[load]);
		}
	}
	
	public double move2Double(int xi, int yi, int xf, int yf)
	{
		return Math.pow(2, xi)*Math.pow(3, yi)*Math.pow(5, xf)*Math.pow(7, yf);
	}
	
	public void makeBestMoveOpp(ChessBoard board, boolean white) //opponent is !white (white is the AI)
	{
		ChessBoard hold = new ChessBoard(board);
		//System.out.println("at least here");
		//board.printBoard();
		int holdValue;
		int bestValue = 0;
		double bestMove = 0;
		
		for(int x = 0; x < 8; x++)
		{
			for(int y = 0; y < 8; y++)
			{
				//System.out.println("position evaluating: "+ x + " " + y);
				if((white && hold.getBoardPieces()[x][y] > 0) || (!white && hold.getBoardPieces()[x][y] < 0))
				{
				//	System.out.println("one of the opponents peices");
					int indexMaxMoves = -1;
					//hold.printBoard();
				//	System.out.println(hold.POI);
					int[] legals = new int[27];
					hold.getLegalTiles(x, y);
				//	System.out.println(hold.POI);
					getMaxMoves: for(int step = 0; step < 27; step++)
					{
						legals[step] = hold.legals[step];
						if(legals[step] != 0)
						{
							indexMaxMoves++;
						}
						else
						{
							break getMaxMoves;
						}
					}
					//System.out.println(legals);
				//	System.out.println(legals[0] + " " + legals[1] + " " + legals[2]);
				//	System.out.println("index be" + indexMaxMoves);
				//	System.out.println("0 less than equal to index "+ (boolean)(0<=indexMaxMoves));
					for(int step = 0; step <= indexMaxMoves; step++)
					{
					//	System.out.println(legals[0] + " " + legals[1] + " " + legals[2]);
						int xf = 0;//System.out.println("know that legals at step is: " + legals[step] + " and legals at 1 be " + legals[1]);
						
						int yf = 0;
						//System.out.println("into the next llop");
						int nextLegal = legals[step];
					//	System.out.println("the value of the next legal "+nextLegal + " at step "+ step);
						while(nextLegal%3==0){nextLegal/=3;xf++;}
						while(nextLegal%5==0){nextLegal/=5;yf++;}
						//System.out.println("final position " + xf +" " + yf);
						//hold.printBoard();
					//	System.out.println("twas the old pos, now here the new");
						hold.moveAI(x, y, xf, yf);
					//	hold.printBoard();
						
						if((holdValue = value(hold,white)) > bestValue)
						{
							bestValue = holdValue;
							bestMove = move2Double(x,y,xf,yf);
						//	System.out.println("best value is: "+bestValue);
						}	
						hold.setBoardValuesEqual(board);
					//	hold.printBoard();
					//	System.out.println("board reset, i hope");
					}
				}//System.out.println("out again");
			}//System.out.println("out close" + x);
		}
		//System.out.println("got out of th eloop");
		makeMove(board, bestMove);
	}
	
	public void makeMove(ChessBoard board, double move)
	{
		int xi = 0;
		int yi = 0;
		int xf = 0;
		int yf = 0;
		//System.out.println("EVEN GETTING HERE" + move);
		while(move%2==0) {move/=2; xi++;}
		while(move%3==0) {move/=3; yi++;}
		while(move%5==0) {move/=5; xf++;}
		while(move%7==0) {move/=7; yf++;}
		
		board.moveAI(xi, yi, xf, yf);
	}
	
	public int getFourthLayerBestValue(ChessBoard board, boolean white)
	{
		//System.out.println("calling, good");
		int bestValue = 0;
		int vroom = 0;
		
		ChessBoard holdAfterOppMove = new ChessBoard(board); //we have passed the method a potential AI move (move 1)
		//System.out.println("about to predict opp next move");
		makeBestMoveOpp(holdAfterOppMove, !white); //we predict the best opponent move
		//holdAfterOppMove.printBoard();
		ChessBoard tester = new ChessBoard(holdAfterOppMove); //here is a new ChessBoard to make future moves
		System.out.println("hello???");
		double[] holdBestEight = new double[8];
		getBestEight(tester, white); //the top 8 second moves for AI
		for(int load = 0; load < 8; load++)
		{
			holdBestEight[load] = bestEight[load]; //load them in
			System.out.println(bestEight[load]);
		}
		//System.out.println("just before the loop");
		for(int move = 0; move < 8; move++)
		{
			tester.setBoardValuesEqual(holdAfterOppMove);
			System.out.println("meh");
			efficient:
			{
				if(holdBestEight[move] == 0.0) {break efficient;} //if there are fewer than 8 moves, ignore the other slots
				
				makeMove(tester, holdBestEight[move]); //we are making each of the next AI moves (move 2)
				tester.printBoard();
				makeBestMoveOpp(tester, !white); //we predict the best opponent move
				ChessBoard tester2 = new ChessBoard(tester); //here is a new Chessboard to make future moves
				double[] holdBestEight2 = new double[8];
				getBestEight(tester2, white); //the top 8 third moves for AI
				for(int load2 = 0; load2 < 8; load2 ++)
				{
					holdBestEight2[load2] = bestEight[load2];
				}
				for(int move2 = 0; move < 8; move++)
				{
					tester2.setBoardValuesEqual(tester);
					efficient2:
					{
						if(holdBestEight2[move2] == 0.0) {break efficient2;}
						
						makeMove(tester2, holdBestEight2[move2]); //we are making each of the next AI moves (move 3)
						makeBestMoveOpp(tester2, !white); // we predict the best opponent move
						ChessBoard tester3 = new ChessBoard(tester2); //here is a new ChessBoard to make future moves
						double[] holdBestEight3 = new double[8];
						getBestEight(tester3, white); //the top 8 fourth moves for AI
						for(int load3 = 0; load3 < 8; load3++)
						{
							holdBestEight3[load3] = bestEight[load3];
						}
						for(int move3 = 0; move3 < 8; move3++)
						{
							vroom++;
							System.out.println("in much far" + vroom);
							tester3.setBoardValuesEqual(tester2);
							efficient3:
							{
								if(holdBestEight3[move3] == 0.0) {break efficient3;}
								
								makeMove(tester3, holdBestEight3[move3]); //we are makinge each of the next AI moves (move 4)
								//at this point, we see the value of the board
								//we adjust bestValue if the value of this board is better than the others
								int curValue = value(tester3, white);
								if(curValue > bestValue)
								{
									bestValue = curValue;
								}
							}
						}System.out.println("you very sloow");
						
					}System.out.println("you slow");
				}
			}
		}
		System.out.println("decieded on: " + bestValue);
		return bestValue;
	}
}

 package game;

import java.util.ArrayList;

public class ChessBoard
{
	//GENERAL BOARD
	int[][] board; //the main virtual game board, where all the action is
	boolean gameOver; //is the game over
	boolean turn; //true: white's turn, false: black's turn
	
	//GAME STATES
	boolean whiteCheck;
	boolean blackCheck;
	boolean stalemate;
	boolean whiteCheckmate;
	boolean blackCheckmate;
	
	//FOR DETERMINING LEGAL MOVES
	int[][] Qlegal; //a queued board to determine if a move is legal
	int[] legals; //list of all legal moves for the piece at the given x and y
	int curInd; //the current indice of legals
	int POI; //piece of interest
	
	//CASTLING DISQUALIFIERS (all are true if castling is allowed)
	boolean wAllowCastleMove; //king has not moved
	boolean bAllowCastleMove;
	boolean wAllowCastleCheck; //king never been in check
	boolean bAllowCastleCheck;
	boolean wRightRookMove; //right rook has not moved	(right and left are respective of the specific color's perspective)
	boolean bRightRookMove;
	boolean wLeftRookMove; //left rook has not moved
	boolean bLeftRookMove;
	
	//EN PASSANT STATUS
	int[] whitePassantables;
	int[] blackPassantables;
	
	//GAME HISTORY
	ArrayList<String> gameHistory;
	
	public ChessBoard()
	{
		board = new int[8][8]; //a chess board is an 8x8 grid
		
		Qlegal = new int[8][8];
		legals = new int[27]; //there is a maximum of 27 possible moves for a fully unhindered queen
		curInd = 0;
		
		whitePassantables = new int[8];
		blackPassantables = new int[8];
		
		gameHistory = new ArrayList<String>();
		
		loadNewGame();
	}
	
	public ChessBoard(ChessBoard b)
	{
		board = new int[8][8];
		
		Qlegal = new int[8][8];
		legals = new int[27];
		
		whitePassantables = new int[8];
		blackPassantables = new int[8];
		
		gameHistory = new ArrayList<String>();
		
		this.setBoardValuesEqual(b);
	}
	
	/*
	 * WHITE
	 * 		1 - pawn
	 * 		2 - rook
	 * 		3 - knight
	 * 		4 - bishop
	 * 		5 - queen
	 * 		6 - king
	 * BLACK
	 * 		-1 - pawn
	 * 		-2 - rook
	 * 		-3 = knight
	 * 		-4 = bishop
	 * 		-5 = queen
	 * 		-6 = king
	 */
	
	public void setBoardValuesEqual(ChessBoard b)
	{
		for(int x = 0; x < 8; x++)
		{
			for(int y = 0; y < 8; y++)
			{
				board[x][y] = b.getBoardPieces()[x][y];
				Qlegal[x][y] = b.Qlegal[x][y];			
			}
			
			whitePassantables[x] = b.whitePassantables[x];
			blackPassantables[x] = b.blackPassantables[x];
		}
		
		for(int step = 0; step < 27; step++)
		{
			legals[step] = b.legals[step];
		}
		
		gameOver = b.gameOver;
		turn = b.getTurn();
		
		whiteCheck = b.whiteCheck();
		blackCheck = b.blackCheck();
		stalemate = b.isStalemate();
		whiteCheckmate = b.whiteCheckmate();
		blackCheckmate = b.blackCheckmate();
		
		curInd = b.curInd;
		POI = b.POI;
		
		wAllowCastleMove = b.wAllowCastleMove;
		bAllowCastleMove = b.bAllowCastleMove;
		wAllowCastleCheck = b.wAllowCastleCheck;
		bAllowCastleCheck = b.bAllowCastleCheck;
		wRightRookMove = b.wRightRookMove;
		bRightRookMove = b.bRightRookMove;
		wLeftRookMove = b.wLeftRookMove;
		bLeftRookMove = b.bLeftRookMove;
		
		gameHistory.clear();
		for(String state : b.getGameHistory())
		{
			this.gameHistory.add(state);
		}
	}
	
	public int[] getLegalTiles(int x, int y)
	{		
		//first clear the legals list from before
		for(int c = 0; c<27; c++)
		{
			legals[c] = 0;
		}
		curInd = 0;
		
		//what piece are we
		POI = board[x][y];
		
		//if we are a white rook
		if(POI == 2)
		{
			getLegalWhiteRook(x,y,true);
		}
		//or a black rook
		else if(POI == -2)
		{
			getLegalWhiteRook(x,y,false);
		}
		//if we are a white bishop
		else if(POI == 4)
		{
			getLegalWhiteBishop(x,y,true);
		}
		//or a black bishop
		else if(POI == -4)
		{
			getLegalWhiteBishop(x,y,false);
		}
		//if we are a white queen
		else if(POI == 5)
		{
			getLegalWhiteQueen(x,y,true);
		}
		//or a black queen
		else if(POI == -5)
		{
			getLegalWhiteQueen(x,y,false);
		}
		//if we are a white king
		else if(POI == 6)
		{
			getLegalWhiteKing(x,y,true);
		}
		else if(POI == -6)
		{
			getLegalWhiteKing(x,y,false);
		}
		//if we are a white knight
		else if(POI == 3)
		{
			getLegalWhiteKnight(x,y,true);
		}
		//or a black knight
		else if(POI == -3)
		{
			getLegalWhiteKnight(x,y,false);
		}
		//if we are a white pawn
		else if(POI == 1)
		{
			getLegalWhitePawn(x,y);
		}
		//or a black pawn
		else if(POI == -1)
		{
			getLegalBlackPawn(x,y);
		}
		//System.out.println("yeet");
		return legals;
	}
	
	public void addLegalRespectCheck(int xi, int yi, int xf, int yf, boolean white)
	{
		updateQlegal(); //update the queued board to the current board
		Qlegal[xf][yf] = POI; //places the piece we are interested in on the queued board where it might be played
		Qlegal[xi][yi] = 0; //clears the tile where the piece we are interested in used to be on the queued board
		
		if(white)
		{
			//runs the queued board to check if white is in check
			if(!whiteCheck(Qlegal))
			{	
				//the location of a legal tile is saved as 3^xPos + 5^yPos
					//which is a unique number for each potential location on the board
					//thus the 2D location is allowed to be saved as an integer for efficiency
				legals[curInd] = (int)(Math.pow(3, xf) * Math.pow(5,yf));
				curInd++; //iterates to the next open slot in the list of legal moves
			}
		}
		else
		{
			if(!blackCheck(Qlegal))
			{
				legals[curInd] = (int)(Math.pow(3, xf) * Math.pow(5,yf));
				curInd++;
			}
		}
	}
	
	public boolean checkLegalRespectCheck(int xi, int yi, int xf, int yf, int piece, boolean white)
	{
		//checks if a certain move is legal (works the same as addLegalRespectCheck, but a manual check)
		updateQlegal();
		Qlegal[xf][yf] = piece;
		Qlegal[xi][yi] = 0;
		
		if(white)
		{
			if(whiteCheck(Qlegal))
			{
				return false;
			}
		}
		else
		{
			if(blackCheck(Qlegal))
			{
				return false;
			}
		}
		return true;
	}
	
//	PAWN MOVES
	public void getLegalWhitePawn(int x, int y)
	{
		//just a precaution for when the pawn gets to the top
		if(y<7)
		{
			//if we are on our first move
			if(y == 1)
			{
				if(board[x][y+1] == 0)
				{
					addLegalRespectCheck(x,y,x,y+1,true);
					if(board[x][y+2] == 0)
					{
						addLegalRespectCheck(x,y,x,y+2,true);
					}
				}
			}
			else
			{
				if(board[x][y+1] == 0)
				{
					addLegalRespectCheck(x,y,x,y+1,true);
				}
			}
			//check if we can take anything
			//to the right
			if(x<7)
			{
				if(board[x+1][y+1] < 0)
				{
					addLegalRespectCheck(x,y,x+1,y+1,true);
				}
			}
			//to the left
			if(x>0)
			{
				if(board[x-1][y+1] < 0)
				{
					addLegalRespectCheck(x,y,x-1,y+1,true);
				}
			}
			//for en passant
			//if my position is far enough forward
			if(y == 4)
			{
				//looking left
				if(x>0)
				{
					if(board[x-1][y] == -1 && blackPassantables[x-1] == 1)
					{
						addLegalRespectCheck(x,y,x-1,y+1,true);
					}
				}
				//looking right
				if(x<7)
				{
					if(board[x+1][y] == -1 && blackPassantables[x+1] == 1)
					{
						addLegalRespectCheck(x,y,x+1,y+1,true);
					}
				}
			}
		}
	}
	public void getLegalBlackPawn(int x, int y)
	{
		//just a precaution for when the pawn gets to the bottom
		if(y>0)
		{
			//if we are on our first move
			if(y == 6)
			{
				if(board[x][y-1] == 0)
				{
					addLegalRespectCheck(x,y,x,y-1,false);
					if(board[x][y-2] == 0)
					{
						addLegalRespectCheck(x,y,x,y-2,false);
					}
				}
			}
			else
			{
				if(board[x][y-1] == 0)
				{
					addLegalRespectCheck(x,y,x,y-1,false);
				}
			}
			//check if we can take anything
			//to the right
			if(x<7)
			{
				if(board[x+1][y-1] > 0)
				{
					addLegalRespectCheck(x,y,x+1,y-1,false);
				}
			}
			//to the left
			if(x>0)
			{
				if(board[x-1][y-1] > 0)
				{
					addLegalRespectCheck(x,y,x-1,y-1,false);
				}
			}
			//for en passant
			//if my position is far enough forward
			if(y == 3)
			{
				//looking left
				if(x>0)
				{
					if(board[x-1][y] == 1 && whitePassantables[x-1] == 1)
					{
						addLegalRespectCheck(x,y,x-1,y-1,false);
					}
				}
				//looking right
				if(x<7)
				{
					if(board[x+1][y] == 1 && whitePassantables[x+1] == 1)
					{
						addLegalRespectCheck(x,y,x+1,y-1,false);
					}
				}
			}
		}
	}
//	KNIGHT MOVES
	public void getLegalWhiteKnight(int x, int y, boolean white)
	{
		//up
		if(y+2<8)
		{
			//left
			if(x-1>=0)
			{
				if((white && board[x-1][y+2] <= 0) || (!white && board[x-1][y+2] >= 0))
				{
					addLegalRespectCheck(x,y,x-1,y+2,white);
				}
			}
			//right
			if(x+1<8)
			{
				if((white && board[x+1][y+2] <= 0) || (!white && board[x+1][y+2] >= 0))
				{
					addLegalRespectCheck(x,y,x+1,y+2,white);
				}
			}
		}
		//down
		if(y-2>=0)
		{
			//left
			if(x-1>=0)
			{
				if((white && board[x-1][y-2] <= 0) || (!white && board[x-1][y-2] >= 0))
				{
					addLegalRespectCheck(x,y,x-1,y-2,white);
				}
			}
			//right
			if(x+1<8)
			{
				if((white && board[x+1][y-2] <= 0) || (!white && board[x+1][y-2] >= 0))
				{
					addLegalRespectCheck(x,y,x+1,y-2,white);
				}
			}
		}
		//left
		if(x-2>=0)
		{
			//up
			if(y+1<8)
			{
				if((white && board[x-2][y+1] <= 0) || (!white && board[x-2][y+1] >= 0))
				{
					addLegalRespectCheck(x,y,x-2,y+1,white);
				}
			}
			//down
			if(y-1>=0)
			{
				if((white && board[x-2][y-1] <= 0) || (!white && board[x-2][y-1] >= 0))
				{
					addLegalRespectCheck(x,y,x-2,y-1,white);
				}
			}
		}
		//right
		if(x+2<8)
		{
			//up
			if(y+1<8)
			{
				if((white && board[x+2][y+1] <= 0) || (!white && board[x+2][y+1] >= 0))
				{
					addLegalRespectCheck(x,y,x+2,y+1,white);
				}
			}
			//down
			if(y-1>=0)
			{
				if((white && board[x+2][y-1] <= 0) || (!white && board[x+2][y-1] >= 0))
				{
					addLegalRespectCheck(x,y,x+2,y-1,white);
				}
			}
		}
	}
//	KING MOVEMENTS
	public void getLegalWhiteKing(int x, int y, boolean white)
	{
		//bottom row
		if(y-1 >= 0)
		{
			//bottom left
			if(x-1 >= 0)
			{
				if(board[x-1][y-1] == 0 || (board[x-1][y-1]>0 && !white) || (board[x-1][y-1]<0 && white))
				{
					addLegalRespectCheck(x,y,x-1,y-1,white);
				}
			}
			//bottom middle
			if(board[x][y-1] == 0 || (board[x][y-1]>0 && !white) || (board[x][y-1]<0 && white))
			{
				addLegalRespectCheck(x,y,x,y-1,white);
			}
			//bottom right
			if(x+1 < 8)
			{
				if(board[x+1][y-1] == 0 || (board[x+1][y-1]>0 && !white) || (board[x+1][y-1]<0 && white))
				{
					addLegalRespectCheck(x,y,x+1,y-1,white);
				}
			}
		}
		//top row
		if(y+1 < 8)
		{
			//top left
			if(x-1 >= 0)
			{
				if(board[x-1][y+1] == 0 || (board[x-1][y+1]>0 && !white) || (board[x-1][y+1]<0 && white))
				{
					addLegalRespectCheck(x,y,x-1,y+1,white);
				}
			}
			//top middle
			if(board[x][y+1] == 0 || (board[x][y+1]>0 && !white) || (board[x][y+1]<0 && white))
			{
				addLegalRespectCheck(x,y,x,y+1,white);
			}
			//top right
			if(x+1 < 8)
			{
				if(board[x+1][y+1] == 0 || (board[x+1][y+1]>0 && !white) || (board[x+1][y+1]<0 && white))
				{
					addLegalRespectCheck(x,y,x+1,y+1,white);
				}
			}
		}
		//center left
		if(x-1 >= 0)
		{
			if(board[x-1][y] == 0 || (board[x-1][y]>0 && !white) || (board[x-1][y]<0 && white))
			{
				addLegalRespectCheck(x,y,x-1,y,white);
			}
		}
		//center right
		if(x+1 < 8)
		{
			if(board[x+1][y] == 0 || (board[x+1][y]>0 && !white) || (board[x+1][y]<0 && white))
			{
				addLegalRespectCheck(x,y,x+1,y,white);
			}
		}
		
		//for white castling
		if(white)
		{
			if(wAllowCastleMove)
			{
				if(wAllowCastleCheck)
				{
					//if the king has not moved and has not been in check
					//check to the right
					if(wRightRookMove)
					{
						if(board[5][0] == 0 && board[6][0] == 0) //if the tiles are clear
						{
							if(checkLegalRespectCheck(4,0,5,0,6,true)) //if the king is not in check in the position he passes
							{
								addLegalRespectCheck(x,y,6,0,true);
							}
						}
					}
					//check to the left
					if(wLeftRookMove)
					{
						if(board[3][0] == 0 && board[2][0] == 0 && board[1][0] == 0) //if the tiles are clear
						{
							if(checkLegalRespectCheck(4,0,3,0,6,true)) //if the king is not in check in the postion he passes
							{
								addLegalRespectCheck(x,y,2,0,true);
							}
						}
					}
				}
			}
		}
		else //for black castling
		{
			if(bAllowCastleMove)
			{
				if(bAllowCastleCheck)
				{
					//if the king has not moved and has not been in check
					//check to the left
					if(bLeftRookMove)
					{
						if(board[5][7] == 0 && board[6][7] == 0) //if the tiles are clear
						{
							if(checkLegalRespectCheck(4,7,5,7,-6,false)) //if the king is not in check in the position he passes
							{
								addLegalRespectCheck(x,y,6,7,false);
							}
						}
					}
					//check to the right
					if(wRightRookMove)
					{
						if(board[3][7] == 0 && board[2][7] == 0 && board[1][7] == 0) //if the tiles are clear
						{
							if(checkLegalRespectCheck(4,7,3,7,-6,false)) //if the king is not in check in the postion he passes
							{
								addLegalRespectCheck(x,y,2,7,false);
							}
						}
					}
				}
			}
		}
	}
//	QUEEN MOVEMENTS
	public void getLegalWhiteQueen(int x, int y, boolean white)
	{
		getLegalWhiteRook(x,y,white);
		getLegalWhiteBishop(x,y,white);
	}
//	BISHOP MOVEMENTS
	public void getLegalWhiteBishop(int x, int y, boolean white)
	{
		int yStep = y+1;
		bishUpRight: for(int dist = x+1;dist<8;dist++)
		{
			
			//makes sure we dont go out of bounds in the y direction
			if(yStep > 7 || yStep < 0)
			{
				break bishUpRight;
			}
			if(board[dist][yStep] == 0)
			{
				addLegalRespectCheck(x,y,dist,yStep,white);
			}
			else if(board[dist][yStep] < 0 && white)
			{
				addLegalRespectCheck(x,y,dist,yStep,white);
				break bishUpRight;
			}
			else if(board[dist][yStep] > 0 && !white)
			{
				addLegalRespectCheck(x,y,dist,yStep,white);
				break bishUpRight;
			}
			else
			{
				break bishUpRight;
			}
			yStep++;
		}
		
		yStep = y+1;
		bishUpLeft: for(int dist = x-1;dist>=0;dist--)
		{
			//makes sure we dont go out of bounds in the y direction
			if(yStep > 7 || yStep < 0)
			{
				break bishUpLeft;
			}
			if(board[dist][yStep] == 0)
			{
				addLegalRespectCheck(x,y,dist,yStep,white);
			}
			else if(board[dist][yStep] < 0 && white)
			{
				addLegalRespectCheck(x,y,dist,yStep,white);
				break bishUpLeft;
			}
			else if(board[dist][yStep] > 0 && !white)
			{
				addLegalRespectCheck(x,y,dist,yStep,white);
				break bishUpLeft;
			}
			else
			{
				break bishUpLeft;
			}
			yStep++;
		}
		
		yStep = y-1;
		bishDownRight: for(int dist = x+1;dist<8;dist++)
		{
			//makes sure we dont go out of bounds in the y direction
			if(yStep > 7 || yStep < 0)
			{
				break bishDownRight;
			}
			if(board[dist][yStep] == 0)
			{
				addLegalRespectCheck(x,y,dist,yStep,white);
			}
			else if(board[dist][yStep] < 0 && white)
			{
				addLegalRespectCheck(x,y,dist,yStep,white);
				break bishDownRight;
			}
			else if(board[dist][yStep] > 0 && !white)
			{
				addLegalRespectCheck(x,y,dist,yStep,white);
				break bishDownRight;
			}
			else
			{
				break bishDownRight;
			}
			yStep--;
		}
		
		yStep = y-1;
		bishDownLeft: for(int dist = x-1;dist>=0;dist--)
		{
			//makes sure we dont go out of bounds in the y direction
			if(yStep > 7 || yStep < 0)
			{
				break bishDownLeft;
			}
			if(board[dist][yStep] == 0)
			{
				addLegalRespectCheck(x,y,dist,yStep,white);
			}
			else if(board[dist][yStep] < 0 && white)
			{
				addLegalRespectCheck(x,y,dist,yStep,white);
				break bishDownLeft;
			}
			else if(board[dist][yStep] > 0 && !white)
			{
				addLegalRespectCheck(x,y,dist,yStep,white);
				break bishDownLeft;
			}
			else
			{
				break bishDownLeft;
			}
			yStep--;
		}
	}
//	ROOK MOVEMENTS	
	public void getLegalWhiteRook(int x, int y, boolean white)
	{
		//up
		rookUp: for(int step = y+1;step < 8; step ++)
		{
			if(board[x][step] == 0)
			{
				//addLegalWhiteRookVert(x,y,step,white);
				addLegalRespectCheck(x,y,x,step,white);
			}
			else if(white && board[x][step] < 0)
			{
				//addLegalWhiteRookVert(x,y,step,white);
				addLegalRespectCheck(x,y,x,step,white);
				break rookUp;
			}
			else if(!white && board[x][step] > 0)
			{
				//addLegalWhiteRookVert(x,y,step,white);
				addLegalRespectCheck(x,y,x,step,white);
				break rookUp;
			}
			else
			{
				break rookUp;
			}
		}
		//down
		rookDown: for(int step = y-1;step>=0; step--)
		{
			if(board[x][step] == 0)
			{
				//addLegalWhiteRookVert(x,y,step,white);
				addLegalRespectCheck(x,y,x,step,white);
			}
			else if(white && board[x][step] < 0)
			{
				//addLegalWhiteRookVert(x,y,step,white);
				addLegalRespectCheck(x,y,x,step,white);
				break rookDown;
			}
			else if(!white && board[x][step] > 0)
			{
				//addLegalWhiteRookVert(x,y,step,white);
				addLegalRespectCheck(x,y,x,step,white);
				break rookDown;
			}
			else
			{
				break rookDown;
			}
		}
		//left
		rookLeft: for(int step = x-1;step>=0; step--)
		{
			if(board[step][y] == 0)
			{
				//addLegalWhiteRookHor(x,y,step,white);
				addLegalRespectCheck(x,y,step,y,white);
			}
			else if(white && board[step][y] < 0)
			{
				//addLegalWhiteRookHor(x,y,step,white);
				addLegalRespectCheck(x,y,step,y,white);
				break rookLeft;
			}
			else if(!white && board[step][y] > 0)
			{
				//addLegalWhiteRookHor(x,y,step,white);
				addLegalRespectCheck(x,y,step,y,white);
				break rookLeft;
			}
			else
			{
				break rookLeft;
			}
		}
		//right
		rookRight: for(int step = x+1;step<8;step++)
		{
			if(board[step][y] == 0)
			{
				//addLegalWhiteRookHor(x,y,step,white);
				addLegalRespectCheck(x,y,step,y,white);
			}
			else if(white && board[step][y] < 0)
			{
				//addLegalWhiteRookHor(x,y,step,white);
				addLegalRespectCheck(x,y,step,y,white);
				break rookRight;
			}
			else if(!white && board[step][y] > 0)
			{
				//addLegalWhiteRookHor(x,y,step,white);
				addLegalRespectCheck(x,y,step,y,white);
				break rookRight;
			}
			else
			{
				break rookRight;
			}
		}
	}
	
	public void updateCastleDisqualifiers()
	{
		//white
		checkWhite: if(wAllowCastleMove)
		{
			if(board[4][0] != 6) //if the king has moved
			{
				wAllowCastleMove = false;
				break checkWhite;
			}
			if(whiteCheck()) //if the king is in check
			{
				wAllowCastleCheck = false;
				break checkWhite;
			}
			if(board[7][0] != 2) //if the right rook has moved
			{
				wRightRookMove = false;
			}
			if(board[0][0] != 2) //if the left rook has moved
			{
				wLeftRookMove = false;
			}
		}
		
		//black
		checkBlack: if(bAllowCastleMove)
		{
			if(board[4][7] != -6) //if the king has moved
			{
				bAllowCastleMove = false;
				break checkBlack;
			}
			if(blackCheck()) //if the king is in check
			{
				bAllowCastleCheck = false;
				break checkBlack;
			}
			if(board[7][7] != -2) //if the left rook has moved
			{
				bLeftRookMove = false;
			}
			if(board[0][7] != -2) //if the right rook has moved
			{
				bRightRookMove = false;
			}
		}
	}
	
	public void updatePassantability()
	{
		//0 - unmoved pawn
		//1 - pawn has moved 2 spaces on first turn and can be passanted
		//2 - pawn can no longer be passanted
		
		for(int xStep = 0; xStep < 8; xStep++)
		{
			//for white
			//if the pawn is unmoved, ignore
			if(board[xStep][1] == 1) {}
			//if the pawn is not passantable, it will never be again
			else if(whitePassantables[xStep] == 2) {}
			//if we were passantable, we no longer are
			else if(whitePassantables[xStep] == 1)
			{
				whitePassantables[xStep] = 2;
			}
			//if the pawn has moved 2 moves on first move
			else if(board[xStep][1] == 0 && board[xStep][2] == 0 && board[xStep][3] == 1)
			{
				whitePassantables[xStep] = 1;
			}
			//if anything else has happened, this pawn can no longer be passanted
			else
			{
				whitePassantables[xStep] = 2;
			}
			//for black
			//if the pawn is unmoved, ignore
			if(board[xStep][6] == -1) {}
			//if the pawn is not passantable, it will never be again
			else if(blackPassantables[xStep] == 2) {}
			//if we were passantable, we no longer are
			else if(blackPassantables[xStep] == 1)
			{
				blackPassantables[xStep] = 2;
			}
			//if the pawn has moved 2 moves on first move
			else if(board[xStep][6] == 0 && board[xStep][5] == 0 && board[xStep][4] == -1)
			{
				blackPassantables[xStep] = 1;
			}
			//if anything else has happened, this pawn can no longer be passanted
			else
			{
				blackPassantables[xStep] = 2;
			}
		}
	}
	
	public boolean whiteCheck(int[][] b)
	{
		//find the king
		int x = -1;
		int y = -1;
		for(int i=0;i<8;i++)
		{
			for(int j=0;j<8;j++)
			{
				if(b[i][j] == 6)
				{
					x = i; y = j;
				}
			}
		}
		int hold;
		checkUp: for(int step = y+1;step<8; step++)
		{
			hold = b[x][step];
			if(hold != -2 && hold != -5 && hold != 0)
			{
				break checkUp;
			}
			if(hold == -2 || hold == -5)
			{
				return true;
			}
		}
		checkDown: for(int step = y-1;step>=0; step--)
		{
			hold = b[x][step];
			if(hold != -2 && hold != -5 && hold != 0)
			{
				break checkDown;
			}
			if(hold == -2 || hold == -5)
			{
				return true;
			}
		}
		checkLeft: for(int step = x-1;step>=0;step--)
		{
			hold = b[step][y];
			if(hold != -2 && hold != -5 && hold != 0)
			{
				break checkLeft;
			}
			if(hold == -2 || hold == -5)
			{
				return true;
			}
		}
		checkRight: for(int step = x+1;step<8;step++)
		{
			hold = b[step][y];
			if(hold != -2 && hold != -5 && hold != 0)
			{
				break checkRight;
			}
			if(hold == -2 || hold == -5)
			{
				return true;
			}
		}
		int yStep = y+1;
		cUpRight: for(int step = x+1;step<8;step++)
		{
			//staying in bounds in y direction
			if(yStep>7)
			{
				break cUpRight;
			}
			hold = b[step][yStep];
			if(hold != -4 && hold != -5 && hold != 0)
			{
				break cUpRight;
			}
			if(hold == -4 || hold == -5)
			{
				return true;
			}
			yStep++;
		}
		yStep = y+1;
		cUpLeft: for(int step = x-1;step>=0;step--)
		{
			if(yStep>7)
			{
				break cUpLeft;
			}
			hold = b[step][yStep];
			if(hold != -4 && hold != -5 && hold != 0)
			{
				break cUpLeft;
			}
			if(hold == -4 || hold == -5)
			{
				return true;
			}
			yStep++;
		}
		yStep = y-1;
		cDownRight: for(int step = x+1;step<8;step++)
		{
			if(yStep<0)
			{
				break cDownRight;
			}
			hold = b[step][yStep];
			if(hold !=-4 && hold != -5 && hold != 0)
			{
				break cDownRight;
			}
			if(hold == -4 || hold == -5)
			{
				return true;
			}
			yStep--;
		}
		yStep = y-1;
		cDownLeft: for(int step = x-1;step>=0;step--)
		{
			if(yStep<0)
			{
				break cDownLeft;
			}
			hold = b[step][yStep];
			if(hold !=-4 && hold != -5 && hold != 0)
			{
				break cDownLeft;
			}
			if(hold == -4 || hold == -5)
			{
				return true;
			}
			yStep--;
		}
		//check for knights
		//up
		if(y+2<8)
		{
			//left
			if(x-1>=0)
			{
				if(b[x-1][y+2] == -3)
				{
					return true;
				}
			}
			//right
			if(x+1<8)
			{
				if(b[x+1][y+2] == -3)
				{
					return true;
				}
			}
		}
		//down
		if(y-2>=0)
		{
			//left
			if(x-1>=0)
			{
				if(b[x-1][y-2] == -3)
				{
					return true;
				}
			}
			//right
			if(x+1<8)
			{
				if(b[x+1][y-2] == -3)
				{
					return true;
				}
			}
		}
		//left
		if(x-2>=0)
		{
			//up
			if(y+1<8)
			{
				if(b[x-2][y+1] == -3)
				{
					return true;
				}
			}
			//down
			if(y-1>=0)
			{
				if(b[x-2][y-1] == -3)
				{
					return true;
				}
			}
		}
		//right
		if(x+2<8)
		{
			//up
			if(y+1<8)
			{
				if(b[x+2][y+1] == -3)
				{
					return true;
				}
			}
			//down
			if(y-1>=0)
			{
				if(b[x+2][y-1] == -3)
				{
					return true;
				}
			}
		}
		
		//pawns
		if(y<7)
		{
			if(x>0)
			{
				if(b[x-1][y+1] == -1)
				{
					return true;
				}
			}
			if(x<7)
			{
				if(b[x+1][y+1] == -1)
				{
					return true;
				}
			}
		}
		
		//king
		//bottom row
				if(y-1 >= 0)
				{
					//bottom left
					if(x-1 >= 0)
					{
						if(b[x-1][y-1] == -6)
						{
							return true;
						}
					}
					//bottom middle
					if(b[x][y-1] == -6)
					{
						return true;
					}
					//bottom right
					if(x+1 < 8)
					{
						if(b[x+1][y-1] == -6)
						{
							return true;
						}
					}
				}
				//top row
				if(y+1 < 8)
				{
					//top left
					if(x-1 >= 0)
					{
						if(b[x-1][y+1] == -6)
						{
							return true;
						}
					}
					//top middle
					if(b[x][y+1] == -6)
					{
						return true;
					}
					//top right
					if(x+1 < 8)
					{
						if(b[x+1][y+1] == -6)
						{
							return true;
						}
					}
				}
				//center left
				if(x-1 >= 0)
				{
					if(b[x-1][y] == -6)
					{
						return true;
					}
				}
				//center right
				if(x+1 < 8)
				{
					if(b[x+1][y] == -6)
					{
						return true;
					}
				}
		
		return false;
	}
	
	public boolean blackCheck(int[][] b)
	{
		//find the king
				int x = -1;
				int y = -1;
				for(int i=0;i<8;i++)
				{
					for(int j=0;j<8;j++)
					{
						if(b[i][j] == -6)
						{
							x = i; y = j;
						}
					}
				}
				int hold;
				checkUp: for(int step = y+1;step<8; step++)
				{
					hold = b[x][step];
					if(hold != 2 && hold != 5 && hold != 0)
					{
						break checkUp;
					}
					if(hold == 2 || hold == 5)
					{
						
						return true;
					}
				}
				checkDown: for(int step = y-1;step>=0; step--)
				{
					hold = b[x][step];
					if(hold != 2 && hold != 5 && hold != 0)
					{
						break checkDown;
					}
					if(hold == 2 || hold == 5)
					{
						return true;
					}
				}
				checkLeft: for(int step = x-1;step>=0;step--)
				{
					hold = b[step][y];
					if(hold != 2 && hold != 5 && hold != 0)
					{
						break checkLeft;
					}
					if(hold == 2 || hold == 5)
					{
						return true;
					}
				}
				checkRight: for(int step = x+1;step<8;step++)
				{
					hold = b[step][y];
					if(hold != 2 && hold != 5 && hold != 0)
					{
						break checkRight;
					}
					if(hold == 2 || hold == 5)
					{
						return true;
					}
				}
				int yStep = y+1;
				cUpRight: for(int step = x+1;step<8;step++)
				{
					//staying in bounds in y direction
					if(yStep>7)
					{
						break cUpRight;
					}
					hold = b[step][yStep];
					if(hold != 4 && hold != 5 && hold != 0)
					{
						break cUpRight;
					}
					if(hold == 4 || hold == 5)
					{
						return true;
					}
					yStep++;
				}
				yStep = y+1;
				cUpLeft: for(int step = x-1;step>=0;step--)
				{
					if(yStep>7)
					{
						break cUpLeft;
					}
					hold = b[step][yStep];
					if(hold != 4 && hold != 5 && hold != 0)
					{
						break cUpLeft;
					}
					if(hold == 4 || hold == 5)
					{
						return true;
					}
					yStep++;
				}
				yStep = y-1;
				cDownRight: for(int step = x+1;step<8;step++)
				{
					if(yStep<0)
					{
						break cDownRight;
					}
					hold = b[step][yStep];
					if(hold != 4 && hold != 5 && hold != 0)
					{
						break cDownRight;
					}
					if(hold == 4 || hold == 5)
					{
						return true;
					}
					yStep--;
				}
				yStep = y-1;
				cDownLeft: for(int step = x-1;step>=0;step--)
				{
					if(yStep<0)
					{
						break cDownLeft;
					}
					hold = b[step][yStep];
					if(hold != 4 && hold != 5 && hold != 0)
					{
						break cDownLeft;
					}
					if(hold == 4 || hold == 5)
					{
						return true;
					}
					yStep--;
				}
				//check for knights
				//up
				if(y+2<8)
				{
					//left
					if(x-1>=0)
					{
						if(b[x-1][y+2] == 3)
						{
							return true;
						}
					}
					//right
					if(x+1<8)
					{
						if(b[x+1][y+2] == 3)
						{
							return true;
						}
					}
				}
				//down
				if(y-2>=0)
				{
					//left
					if(x-1>=0)
					{
						if(b[x-1][y-2] == 3)
						{
							return true;
						}
					}
					//right
					if(x+1<8)
					{
						if(b[x+1][y-2] == 3)
						{
							return true;
						}
					}
				}
				//left
				if(x-2>=0)
				{
					//up
					if(y+1<8)
					{
						if(b[x-2][y+1] == 3)
						{
							return true;
						}
					}
					//down
					if(y-1>=0)
					{
						if(b[x-2][y-1] == 3)
						{
							return true;
						}
					}
				}
				//right
				if(x+2<8)
				{
					//up
					if(y+1<8)
					{
						if(b[x+2][y+1] == 3)
						{
							return true;
						}
					}
					//down
					if(y-1>=0)
					{
						if(b[x+2][y-1] == 3)
						{
							return true;
						}
					}
				}
				
				//pawns
				if(y>0)//check
				{
					if(x>0)
					{
						if(b[x-1][y-1] == 1)
						{
							return true;
						}
					}
					if(x<7)
					{
						if(b[x+1][y-1] == 1)
						{
							return true;
						}
					}
				}
				
				//king
				//bottom row
						if(y-1 >= 0)
						{
							//bottom left
							if(x-1 >= 0)
							{
								if(b[x-1][y-1] == 6)
								{
									return true;
								}
							}
							//bottom middle
							if(b[x][y-1] == 6)
							{
								return true;
							}
							//bottom right
							if(x+1 < 8)
							{
								if(b[x+1][y-1] == 6)
								{
									return true;
								}
							}
						}
						//top row
						if(y+1 < 8)
						{
							//top left
							if(x-1 >= 0)
							{
								if(b[x-1][y+1] == 6)
								{
									return true;
								}
							}
							//top middle
							if(b[x][y+1] == 6)
							{
								return true;
							}
							//top right
							if(x+1 < 8)
							{
								if(b[x+1][y+1] == 6)
								{
									return true;
								}
							}
						}
						//center left
						if(x-1 >= 0)
						{
							if(b[x-1][y] == 6)
							{
								return true;
							}
						}
						//center right
						if(x+1 < 8)
						{
							if(b[x+1][y] == 6)
							{
								return true;
							}
						}
		
		return false;
	}
	
	public boolean whiteCheck()
	{
		return whiteCheck(board);
	}
	
	public boolean blackCheck()
	{
		return blackCheck(board);
	}
	
	public void checkGameState()
	{
		gameOver = false;
		whiteCheck = whiteCheck();
		blackCheck = blackCheck();
		whiteCheckmate = whiteCheckmate();
		blackCheckmate = blackCheckmate();
		stalemate = isStalemate();
	}
	
	public void simplyMove(int x, int y, int fx, int fy)
	{
		board[fx][fy] = board[x][y];
		board[x][y] = 0;
	}
	
	public void move(int x, int y, int fx, int fy)
	{
		//if the space we are moving from is the spot where the king usually sits, check for castling
		onlyMoveOnce:
		{
			if(x == 4 && y == 0)//check for white king spot
			{
				if(board[4][0] == 6) //if that is indeed the white king
				{
					if(fx == 6 && fy == 0) //is this castle to the right
					{
						simplyMove(x,y,fx,fy);
						simplyMove(7,0,5,0);
						break onlyMoveOnce;
					}
					
					else if(fx == 2 && fy == 0) //is this castle to the left
					{
						simplyMove(x,y,fx,fy);
						simplyMove(0,0,3,0);
						break onlyMoveOnce;
					}
				}
			}
			else if(x == 4 && y == 7)//check for black king spot
			{
				if(board[4][7] == -6) //if that is indeed the black king
				{
					if(fx == 6 && fy == 7) //is this castle to the left
					{
						simplyMove(x,y,fx,fy);
						simplyMove(7,7,5,7);
						break onlyMoveOnce;
					}
					else if(fx == 2 && fy == 7) //is this castle to the right
					{
						simplyMove(x,y,fx,fy);
						simplyMove(0,7,3,7);
						break onlyMoveOnce;
					}
				}
			}
		
			//this checks for en passant
			//for white
			//if the piece is a pawn moving to a blank space that is not on the same column which the pawn started, it must be passant
			if(y == 4 && board[x][y] == 1 && board[fx][fy] == 0 && x!=fx)
			{
				simplyMove(x,y,fx,fy);
				board[fx][y] = 0;
				break onlyMoveOnce;
			}
			//for black
			//if the piece is a pawn moving to a blank space that is not on the same column which the pawn started, it must be passant
			else if(y == 3 && board[x][y] == -1 && board[fx][fy] == 0 && x!=fx)
			{
				simplyMove(x,y,fx,fy);
				board[fx][y] = 0;
				break onlyMoveOnce;
			}
			
			//this is for all other (normal) moves
			simplyMove(x,y,fx,fy);
		}
		
		//this is for promotion of pawns to queens, will fix later
		for(int xStep = 0;xStep<=7;xStep++)
		{
			if(board[xStep][0] == -1)
			{
				board[xStep][0] = -5;
			}
			if(board[xStep][7] == 1)
			{
				board[xStep][7] = 5;
			}
		}
		updateCastleDisqualifiers();
		updatePassantability();
		turn = !turn;
		whiteCheck = false;
		blackCheck = false;
		checkGameState();
		recordMove();
	}
	
	public void recordMove()
	{
		String boardState = "";
		//all the pieces for all the positions
		for(int x = 0; x < 8; x++)
		{
			for(int y = 0; y < 8; y++)
			{
				boardState += (board[x][y] + 16);
			}
		}
		
		// records castling data
		boardState += CBTS(wAllowCastleMove)
					+CBTS(bAllowCastleMove)
					+CBTS(wAllowCastleCheck)
					+CBTS(bAllowCastleCheck)
					+CBTS(wRightRookMove)
					+CBTS(bRightRookMove)
					+CBTS(wLeftRookMove)
					+CBTS(bLeftRookMove);
		
		//records the passantability of the pawns
		for(int pass = 0; pass < 8; pass++)
		{
			boardState += CITS(whitePassantables[pass]); //for white
			boardState += CITS(blackPassantables[pass]); //for black
		}
		
		//writes the next turn and goes to the next line
		boardState += CBTS(turn);
		gameHistory.add(boardState);
	}
	
	public String CBTS(boolean b) //(Convert Boolean To String)
	{
		if(b){return "t";}return "f";
	}
	public boolean CSTB(String s) //(Convert String To Boolean)
	{
		if(s.equals("t")){return true;}return false;
	}
	public String CITS(int i) //(Convert Integer to String)
	{
		if(i == 0) {return "0";}return "1";
	}
	public int CSTI(String s) //(Convert String to Integer)
	{
		if(s.equals("0")) {return 0;}return 1;
	}
	
	public void setHistory(ArrayList<String> history)
	{
		gameHistory.clear();
		for(String boardState : history)
		{
			gameHistory.add(boardState);
		}
		loadSavedMove(history.size() - 1);
	}
	
	public ArrayList<String> getGameHistory()
	{
		return gameHistory;
	}
	
	public void loadSavedMove(int pos)
	{
		String gameState = gameHistory.get(pos);
		int index = 0;
		for(int x = 0; x < 8; x++)
		{
			for(int y = 0; y < 8; y++)
			{
				board[x][y] = Integer.parseInt(gameState.substring(index,index + 2)) - 16;
				index += 2;
			}
		}
		wAllowCastleMove = CSTB(gameState.substring(index,++index));
		bAllowCastleMove = CSTB(gameState.substring(index,++index));
		wAllowCastleCheck = CSTB(gameState.substring(index,++index));
		bAllowCastleCheck = CSTB(gameState.substring(index,++index));
		wRightRookMove = CSTB(gameState.substring(index,++index));
		wLeftRookMove = CSTB(gameState.substring(index,++index));
		bRightRookMove = CSTB(gameState.substring(index,++index));
		bLeftRookMove = CSTB(gameState.substring(index,++index));
		
		//set the passantability of pawns
		for(int pass = 0; pass < 8; pass++)
		{
			whitePassantables[pass] = CSTI(gameState.substring(index,++index));
			blackPassantables[pass] = CSTI(gameState.substring(index,++index));
		}
		
		//tells which player plays next
		turn = CSTB(gameState.substring(index));
	}
	
	public void undo()
	{
		int holdSize = gameHistory.size();
		if(!(holdSize == 0))
		{
			if(holdSize == 1)
			{
				loadNewGame();
			}
			else
			{
				loadSavedMove(holdSize-2);
				gameHistory.remove(holdSize-1);
			}
		}
	}
	
	public boolean whiteCheckmate()
	{
		//for all tiles on the board that have white piece
		//get number of legal moves cummulative
		//if no moves, is checkmate
		if(whiteCheck)
		{
			int[] holdLegal = new int[27];
			for(int i = 0; i < 8; i++)
			{
				for(int j = 0; j < 8; j++)
				{
					if(board[i][j] > 0)
					{
						holdLegal = getLegalTiles(i,j);
						if(holdLegal[0] != 0)
						{
							return false;
						}
					}
				}
			}
			gameOver = true;
			return true;
		}
		return false;
	}
	
	public boolean blackCheckmate()
	{
		if(blackCheck)
		{
			int[] holdLegal = new int[27];
			for(int i = 0; i < 8; i++)
			{
				for(int j = 0; j < 8; j++)
				{
					if(board[i][j] < 0)
					{
						holdLegal = getLegalTiles(i,j);
						if(holdLegal[0] != 0)
						{
							return false;
						}
					}
				}
			}
			gameOver = true;
			return true;
		}
		return false;
	}
	
	public boolean isStalemate()
	{
		//5-fold repetition
		//no capture or pawn move in last 75 moves
		//insufficient material - done
		//can't move and not in check - done
		
		//maybe 5 fold repetition and no capture/ pawn move in last 75 moves will be reserved for another version
		//if working with AI, will just have to tell AI to call it a draw; humans will have to decide for themselves
		
		insufficient:
		{
			int numWBish = 0;
			int numBBish = 0;
			int numKnight = 0;
			for(int x = 0;x<8;x++)
			{
				for(int y = 0;y<8;y++)
				{
					int g = board[x][y];
					//if there is a pawn, rook, or queen anywhere on the board
					if(g == 1 || g == 2 || g == 5 || g == -1 || g == -2 || g == -5)
					{
						break insufficient;
					}
					else if(g == 4)
					{
						numWBish++;
					}
					else if(g == -4)
					{
						numBBish++;
					}
					else if(g == 3 || g == -3)
					{
						numKnight++;
					}
				}
			}
			//if there is more than 1 knight, the game can theoretically be won
			if(numKnight > 1)
			{
				break insufficient;
			}
			//if there is 1 knight, and any number of bishops, the game can theoretically be won
			if(numKnight == 1)
			{
				if((numWBish + numBBish) >= 1)
				{
					break insufficient;
				}
				//if there is only 1 knight on the board, the game cannot be won
				gameOver = true;
				return true;
			}
			//if there are no knights, must look at bishops
			if(numKnight == 0)
			{
				//if anyone has 2 bishops, the game is able to be won
				if(numWBish == 2 || numBBish == 2)
				{
					break insufficient;
				}
				//at this point, it is known that there is no more than 1 bishop of each color on the board
				//if there is 1 or 0 bishops on the board, will save the trouble of checking color of squares
				if((numWBish + numBBish) <= 1)
				{
					gameOver = true;
					return true;
				}
				//it is known that each player has exactly 1 bishop
				//if the bishops are on different color squares, a win is theoretically possible
				//get position of both bishops
				int x1 = 0;
				int x2 = 0;
				int y1 = 0;
				int y2 = 0;
				for(int x = 0;x<8;x++)
				{
					for(int y = 0;y<8;y++)
					{
						if(board[x][y] == 4)
						{
							x1 = x;
							y1 = y;
						}
						if(board[x][y] == -4)
						{
							x2 = x;
							y2 = y;
						}
					}
				}
				//continue
				//if the cords add together, an even number coresponds to a black square, while an odd number coresponds to a black square
				//2 even numbers or 2 odd numbers add together to make an even number, while an even and an odd number add to make an odd
				//thus, if the four cords are added together, and the result is an odd number, the bishops are on different color squares
				if((x1 + x2 + y1 + y2) % 2 == 1)
				{
					break insufficient;
				}
				//the bishops are on the same color squares; a win is impossible
				else
				{
					gameOver = true;
					return true;
				}
			}
		}
		
		//can't move and not in check
		//if it's white's turn
		if(turn)
		{
			for(int x = 0;x<8;x++)
			{
				for(int y = 0;y<8;y++)
				{
					if(board[x][y] > 0)
					{
						if(getLegalTiles(x,y)[0] != 0)
						{
							return false;
						}
					}
				}
			}
		}
		else //if it's black's turn
		{
			for(int x = 0;x<8;x++)
			{
				for(int y = 0;y<8;y++)
				{
					if(board[x][y] < 0)
					{
						if(getLegalTiles(x,y)[0] != 0)
						{
							return false;
						}
					}
				}
			}
		}
		gameOver = true;
		return true;
	}
	
	public void adminPlace(int piece, int x, int y)
	{
		board[x][y] = piece;
	}
	
	public void place(String pie, int x, int y, boolean white)
	{
		int p;
		switch(pie)
		{
		case "pawn": p = 1; break;
		case "rook": p = 2; break;
		case "knight": p = 3; break;
		case "bishop": p = 4; break;
		case "queen": p = 5; break;
		case "king": p = 6; break;
		default: p=0; break;
		}
		if(!white)
		{
			p*=-1;
		}
		board[x-1][y-1] = p;
	}
	
	public void placeW(String pie, int x, int y)
	{
		place(pie,x,y,true);
	}
	
	public void placeB(String pie, int x, int y)
	{
		place(pie,x,y,false);
	}
	
	public void loadNewGame()
	{
		resetBoard();
		populateBoard();
	}
	
	public void populateBoard()
	{
		//places pawns first
		for(int s=1;s<=8;s++)
		{
			placeW("pawn",s,2);
			placeB("pawn",s,7);
		}
		
		//rooks
		placeW("rook",1,1);
		placeW("rook",8,1);
		placeB("rook",1,8);
		placeB("rook",8,8);
		//knights
		placeW("knight",2,1);
		placeW("knight",7,1);
		placeB("knight",2,8);
		placeB("knight",7,8);
		//bishops
		placeW("bishop",3,1);
		placeW("bishop",6,1);
		placeB("bishop",3,8);
		placeB("bishop",6,8);
		//queens
		placeW("queen",4,1);
		placeB("queen",4,8);
		//kings
		placeW("king",5,1);
		placeB("king",5,8);
	}

	public void resetBoard()
	{
		gameOver = false;
		whiteCheck = false;
		blackCheck = false;
		stalemate = false;
		whiteCheckmate = false;
		blackCheckmate = false;
		
		wAllowCastleMove = true;
		bAllowCastleMove = true;
		wAllowCastleCheck = true;
		bAllowCastleCheck = true;
		wRightRookMove = true;
		wLeftRookMove = true;
		bRightRookMove = true;
		bLeftRookMove = true;
		
		turn = true;
		
		for(int i = 0;i<8;i++)
		{
			for(int j = 0;j<8;j++)
			{
				board[i][j] = 0;
			}
			whitePassantables[i] = 0;
			blackPassantables[i] = 0;
		}
		gameHistory.clear();
	}
	
	public int[][] getBoardPieces()
	{
		return board;
	}
	
	public int getPieceAt(int x, int y)
	{
		return board[x][y];
	}
	
	public void printBoard()
	{
		for(int y = 7; y>=0; y--)
		{
			for(int x = 0; x<8; x++)
			{
				System.out.print(board[x][y]);
			}
			System.out.println("");
		}
	}
	
	public boolean getTurn()
	{
		return turn;
	}
	
	public void nextTurn()
	{
		turn = !turn;
	}
	
	public void updateQlegal()
	{
		for(int x = 0;x<8;x++)
		{
			for(int y = 0;y<8;y++)
			{
				Qlegal[x][y] = board[x][y];
			}
		}
	}
	
	public void performRandomBlackMove()
	{
		//go to a random tile
		//if it's black, get its moves
		//choose a random move
		
		if(!gameOver)
		{
			int rX = -1;
			int rY = -1;
			int[] allMoves = new int[27];
			int indexMaxMoves = -1;
			while(indexMaxMoves == -1)
			{
				getBlack: while(true)
				{
					rX = (int)(Math.random()*8);
					rY = (int)(Math.random()*8);
					if(board[rX][rY] < 0)
					{
						break getBlack;
					}
				}
				
				allMoves = getLegalTiles(rX,rY);
				
				
				getMaxMoves: for(int step = 0; step < 27; step++)
				{
					if(allMoves[step] != 0)
					{
						indexMaxMoves++;
					}
					else
					{
						break getMaxMoves;
					}
				}
			}
			int zMoveIndex = (int)(Math.random()*(indexMaxMoves + 1));
			int zMove = allMoves[zMoveIndex];
			
			int xPos = 0; int yPos = 0;
			while(zMove%3==0){zMove/=3;xPos++;}
			while(zMove%5==0){zMove/=5;yPos++;}
			
			move(rX,rY,xPos,yPos);
		}
	}
	//public int getGameState() {}
	//public boolean isTileOccupied(int x, int y) {}
	
}

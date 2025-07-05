package game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import system.TEditor;

@SuppressWarnings("serial")
public class Chess_RenderingEngine extends JPanel implements ActionListener,KeyListener,MouseListener
{
	JFrame window; //the window onto which the game will be rendered
	Timer clock; //the timer that governs update speed
	ChessBoard Zboard; //the ChessBoard we are working with
	ChessBoardAI lilTimmy; //the AI that will run black in single player
	ChessBoardAI bigBadBoy;
	
	String filePath; //the filepath for the history txt files
	TEditor sHistory, mHistory; //connections to the txt files we will be saving the games to
	
	int Qwidth; //the queued width of the window
	int Qheight; //the queued height of the window
	
	int xCordBack; //the x cord of the top left of the white board
	int yCordBack; // the y cord of the top left of the white board
	int BackLength; //the full length of the square board
	
	int[][] Qpieces; //potentially will phase out, a (copy?) of the board from the ChessBoard
	
	int xTileClick, yTileClick; //the queued clicked tile
	boolean tileClicked; //have we clicked a tile yet
	boolean clickedOOB; //did we just click Out Of Bounds
	
	boolean undoPressed; //if undo is pressed, so that we do not spam undo
	boolean ctrlPressed; //if ctrl is pressed
	
	boolean viewing; //at endgame, are we viewing the board without going to endgame screen
	
	Color white = new Color(182,149,106);
	Color black = new Color(104,52,0);//(80,40,0);
	Color capture = new Color(243,39,63);
	Color moveable = new Color(128,250,247);
	
	//all the Images
	Image wKing;
	Image wQueen;
	Image wRook;
	Image wKnight;
	Image wBishop;
	Image wPawn;
	Image bKing;
	Image bQueen;
	Image bRook;
	Image bKnight;
	Image bBishop;
	Image bPawn;
	
	//homescreen
	Image homeBackground;//
	Image singlePlayer;
	Image multiPlayer;
	Image settings;
	Image credits;
	
	Image boardBackground;//
	
	Image endgameBackground;
	Image whiteVictory;
	Image blackVictory;
	Image stalemate;
	
	Image promptBackground;//
	Image resume;
	Image startNew;
	Image homeNsave;
	Image homeNdiscard;
	Image viewBoard;
	Image returnHome;
	
	Image back;
	Image creditsThemselves;

	
	//determines if we flip the board for multiplayer or singleplayer
	boolean flip;
	
	//what screen are we on
	int screen; //1 - home
				//2 - multiplayer board
				//3 - single player board
				//4 - resume multiplayer game prompt
				//5 - resume single player game prompt
				//6 - return to homescreen from single player prompt
				//7 - return to homescreen from multiplayer prompt
				//8 - singleplayer endgame return to homescreen
				//9 - multiplayer endgame return to homescreen
				//10 - settings
				//11 - credits
	
	public Chess_RenderingEngine()
	{
		Zboard = new ChessBoard();
		lilTimmy = new ChessBoardAI(Zboard,false);
		bigBadBoy = new ChessBoardAI(Zboard,true);
		
		filePath = "C:\\Users\\ncleo\\Desktop";
		sHistory = new TEditor(filePath + "\\SavedSinglePlayerMoves.txt");
		mHistory = new TEditor(filePath + "\\SavedMultiPlayerMoves.txt");
		
		Qwidth = 10;
		Qwidth = 10;
		Qpieces = new int[8][8];
		
		window = new JFrame("Chess");
		
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible(true);
		window.setSize(500,500);
		
		window.add(this);
		window.addKeyListener(this);
		window.addMouseListener(this);
		
		generateImagesFromSourceFolder();
		
		flip = false;
		viewing = false;
		
		undoPressed = false;
		ctrlPressed = false;
		
		screen = 1;
		
		clock = new Timer(10,this);
		clock.start();
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		this.repaint();
	}
	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		if(screen == 1)
		{
			renderHomeScreen(g);
		}
		else if(screen == 2)
		{
			renderGameBoard(g);
			if(Zboard.gameOver == true && !viewing)
			{
				screen = 9;
			}
		}
		else if(screen == 3)
		{
			renderGameBoard(g);
			if(Zboard.gameOver == true && !viewing)
			{
				screen = 8;
			}
			if(!Zboard.getTurn())
			{
				//System.out.println("it's black's turn");
				//Zboard.performRandomBlackMove();
				lilTimmy.AIturn();
			}
			else {bigBadBoy.play(4);}
		}
		else if(screen == 4 || screen == 5)
		{
			renderResumePrompt(g);
		}
		else if(screen == 6 || screen == 7)
		{
			renderReturnHomePrompt(g);
		}
		else if(screen == 8 || screen == 9)
		{
			renderEndgamePrompt(g);
		}
		else if(screen == 10)
		{
			renderSettings(g);
		}
		else if(screen == 11)
		{
			renderCredits(g);
		}
	}
	
	public void renderSettings(Graphics g)
	{
		g.drawImage(homeBackground, 0, 0, Qwidth, Qheight, null);
		resizeIfNeed();
		g.drawImage(back, xCordBack + BackLength/4, yCordBack + 3*BackLength/4, BackLength/2, BackLength/8, null);
		
	}
	
	public void renderCredits(Graphics g)
	{
		g.drawImage(homeBackground, 0, 0, Qwidth, Qheight, null);
		resizeIfNeed();
		//these are the credits
		g.drawImage(creditsThemselves, xCordBack, yCordBack, BackLength, 7*BackLength/16, null);
		g.drawImage(back, xCordBack + BackLength/4, yCordBack + 3*BackLength/4, BackLength/2, BackLength/8, null);
	}
	
	public void renderEndgamePrompt(Graphics g)
	{
		//this.setBackground(Color.white);
		g.drawImage(endgameBackground, 0, 0, Qwidth, Qheight, null);
		resizeIfNeed();
		if(Zboard.blackCheckmate())
		{
			//show white win
			g.drawImage(whiteVictory, xCordBack, yCordBack, BackLength, BackLength/4, null);
		}
		else if(Zboard.whiteCheckmate())
		{
			//show black win
			g.drawImage(blackVictory, xCordBack, yCordBack, BackLength, BackLength/4, null);
		}
		else
		{
			//show stalemate
			g.drawImage(stalemate, xCordBack, yCordBack, BackLength, BackLength/4, null);
		}
		//g.drawImage(promptBackground, 0, 0, Qwidth, Qheight, null);
		//g.fillRect(xCordBack + BackLength/8, yCordBack + BackLength/4, BackLength - BackLength/4, BackLength/8);
		//g.fillRect(xCordBack + BackLength/8, yCordBack + BackLength/2, BackLength - BackLength/4, BackLength/8);
		g.drawImage(viewBoard, xCordBack + BackLength/8, yCordBack + BackLength/4, BackLength - BackLength/4, BackLength/8, null);
		g.drawImage(returnHome, xCordBack + BackLength/8, yCordBack + BackLength/2, BackLength - BackLength/4, BackLength/8, null);
	}
	
	//public void renderEndgamePromptS(Graphics g)
	//{
		//this.setBackground(Color.black);
		//resizeIfNeed();
		//g.drawImage(promptBackground, Qwidth, Qheight, null);
		//g.fillRect(xCordBack + BackLength/8, yCordBack + BackLength/4, BackLength - BackLength/4, BackLength/8);
		//g.fillRect(xCordBack + BackLength/8, yCordBack + BackLength/2, BackLength - BackLength/4, BackLength/8);
		//g.drawImage(viewBoard, xCordBack + BackLength/8, yCordBack + BackLength/4, BackLength - BackLength/4, BackLength/8, null);
		//g.drawImage(returnHome, xCordBack + BackLength/8, yCordBack + BackLength/2, BackLength - BackLength/4, BackLength/8, null);
	//}
	
	public void renderReturnHomePrompt(Graphics g)
	{
		//this.setBackground(Color.cyan);
		g.drawImage(promptBackground, 0, 0, Qwidth, Qheight, null);
		resizeIfNeed();
		//g.drawImage(promptBackground, 0, 0, Qwidth, Qheight, null);
		//g.fillRect(xCordBack + BackLength/8, yCordBack + BackLength/8, BackLength - BackLength/4, BackLength/8);
		//g.fillRect(xCordBack + BackLength/8, yCordBack + (BackLength/8)*3, BackLength - BackLength/4, BackLength/8);
		//g.fillRect(xCordBack + BackLength/8, yCordBack + (BackLength/8)*5, BackLength - BackLength/4, BackLength/8);
		g.drawImage(resume, xCordBack + BackLength/8, yCordBack + BackLength/8, BackLength - BackLength/4, BackLength/8, null);
		g.drawImage(homeNsave, xCordBack + BackLength/8, yCordBack + (BackLength/8)*3, BackLength - BackLength/4, BackLength/8, null);
		g.drawImage(homeNdiscard, xCordBack + BackLength/8, yCordBack + (BackLength/8)*5, BackLength - BackLength/4, BackLength/8, null);
	}
	
	//public void renderReturnHomeSinglePlayerPrompt(Graphics g)
	//{
		//this.setBackground(Color.yellow);
		//resizeIfNeed();
		//g.drawImage(promptBackground, Qwidth, Qheight, null);
		//g.fillRect(xCordBack + BackLength/8, yCordBack + BackLength/8, BackLength - BackLength/4, BackLength/8);
		//g.fillRect(xCordBack + BackLength/8, yCordBack + (BackLength/8)*3, BackLength - BackLength/4, BackLength/8);
		//g.fillRect(xCordBack + BackLength/8, yCordBack + (BackLength/8)*5, BackLength - BackLength/4, BackLength/8);
		//g.drawImage(resume, xCordBack + BackLength/8, yCordBack + BackLength/8, BackLength - BackLength/4, BackLength/8, null);
		//g.drawImage(homeNsave, xCordBack + BackLength/8, yCordBack + (BackLength/8)*3, BackLength - BackLength/4, BackLength/8, null);
		//g.drawImage(homeNdiscard, xCordBack + BackLength/8, yCordBack + (BackLength/8)*5, BackLength - BackLength/4, BackLength/8, null);
	//}
	
	public void renderResumePrompt(Graphics g)
	{
		//this.setBackground(Color.ORANGE);
		g.drawImage(promptBackground, 0, 0, Qwidth, Qheight, null);
		resizeIfNeed();
		//g.drawImage(promptBackground, 0, 0, Qwidth, Qheight, null);
		//g.fillRect(xCordBack + BackLength/8, yCordBack + BackLength/4, BackLength - BackLength/4, BackLength/8);
		//g.fillRect(xCordBack + BackLength/8, yCordBack + BackLength/2, BackLength - BackLength/4, BackLength/8);
		g.drawImage(resume,xCordBack + BackLength/8, yCordBack + BackLength/4, BackLength - BackLength/4, BackLength/8,null);
		g.drawImage(startNew,xCordBack + BackLength/8, yCordBack + BackLength/2, BackLength - BackLength/4, BackLength/8,null);
	}
	
	//public void renderResumeMultiPlayerPrompt(Graphics g)
	//{
	//	this.setBackground(Color.red);
	//	resizeIfNeed();
	//	g.fillRect(xCordBack + BackLength/8, yCordBack + BackLength/4, BackLength - BackLength/4, BackLength/8);
	//	g.fillRect(xCordBack + BackLength/8, yCordBack + BackLength/2, BackLength - BackLength/4, BackLength/8);
	//}
	
	public void renderGameBoard(Graphics g)
	{
		this.setBackground(Color.gray);
		g.drawImage(homeBackground, 0, 0, Qwidth, Qheight, null);
		drawBoardBackground(g);
		renderAvaliableLocations(g);
		drawPieces(g);
	}
	
	public void renderHomeScreen(Graphics g)
	{
		//this.setBackground(Color.green);
		g.drawImage(homeBackground, 0, 0, Qwidth, Qheight, null);
		resizeIfNeed();
		//a button for single-player
		g.drawImage(singlePlayer, xCordBack + BackLength/8, yCordBack + BackLength/4, 3*BackLength/4, BackLength/8, null);
		//g.setColor(Color.darkGray);
		//g.fillRect(xCordBack, yCordBack + BackLength/4, BackLength, BackLength/8);
		//a button for single-player
		g.drawImage(multiPlayer,xCordBack + BackLength/8, yCordBack + BackLength/2,  3*BackLength/4, BackLength/8,null);
		g.drawImage(settings, xCordBack + BackLength/8, yCordBack + 3*BackLength/4, 5*BackLength/16, BackLength/8, null);
		g.drawImage(credits, xCordBack + BackLength/8 + 7*BackLength/16, yCordBack + 3*BackLength/4, 5*BackLength/16, BackLength/8, null);
		
	}
	
	public void renderAvaliableLocations(Graphics g)
	{
		if(tileClicked)
		{
			//get all the legal moves for this piece
			int index = 0;
			int[] legals = Zboard.getLegalTiles(xTileClick, yTileClick);
			while(legals[index] != 0 && index<27)
			{
				int nextLegal = legals[index];
				int xPos = 0; int yPos = 0;
				while(nextLegal%3==0){nextLegal/=3;xPos++;}
				while(nextLegal%5==0){nextLegal/=5;yPos++;}
				int adjustedX;
				int adjustedY;
				if(!Zboard.getTurn() && flip)
				{
					adjustedX = 7-xPos;
					adjustedY = yPos;
				}
				else
				{
					adjustedX = xPos;
					adjustedY = 7-yPos;
				}
				if(Zboard.board[xPos][yPos] != 0)
				{
					g.setColor(capture);
				}
				else
				{
					g.setColor(moveable);
				}
				g.fillOval(xCordBack + adjustedX*(BackLength/8) + 5, yCordBack + adjustedY*(BackLength/8) + 5, (BackLength/8) - 10, (BackLength/8) - 10);
				index++;
			}
		}
	}
	
	public void drawPieces(Graphics g)
	{
		int adjustedX;
		int adjustedY;
		//need to rethink if this actually optimizes anything
		if(!(Zboard.getBoardPieces().equals(Qpieces)))
		{
			Qpieces = Zboard.getBoardPieces();
		}
		for(int x = 0;x<8; x++)
		{
			for(int y = 0;y<8; y++)
			{
				//if its black's turn and we are flipping, otherwise its white's view
				if(!Zboard.getTurn() && flip)
				{
					adjustedY = y; //even though we count bottom left as (1,1), the computer renders (1,1) as top left
					adjustedX = 7-x;
				}
				else
				{
					adjustedY = 7-y;
					adjustedX = x;
				}
				
				int xCord = xCordBack + adjustedX*(BackLength/8) + 5;
				int yCord = yCordBack + adjustedY*(BackLength/8) + 5;
				int width = (BackLength/8) - 10;
				g.setColor(Color.green);
				switch(Qpieces[x][y])
				{
				case 1: g.drawImage(wPawn,xCord,yCord+(width/16),width,width,null);break;//g.drawString("pawn", xCord, yCord);break; //g.setColor(Color.red);g.fillRect(xCordBack + x*(BackLength/8) + 5, yCordBack + inverse*(BackLength/8) + 5, 10, 10);break;
				case 2: g.drawImage(wRook,xCord,yCord,width,width,null);break;//g.drawString("rook", xCord, yCord);break;
				case 3: g.drawImage(wKnight,xCord,yCord,width,width,null);break;//g.drawString("knight", xCord, yCord);break;
				case 4: g.drawImage(wBishop,xCord,yCord,width,width,null);break;//g.drawString("bishop", xCord, yCord); break;
				case 5: g.drawImage(wQueen,xCord,yCord,width,width,null);break;//g.drawString("queen", xCord, yCord);break;//g.drawImage(te, xCord, yCord, width, width, null);break;
				case 6: if(Zboard.whiteCheck()) {g.setColor(Color.red);g.fillRect(xCord-5,yCord-5,width+10,width+10);}g.drawImage(wKing,xCord,yCord,width,width,null);break;//g.drawString("king", xCord, yCord);break;//g.drawImage(te, xCord, yCord, width, width, null);break;
				case -1: g.drawImage(bPawn,xCord,yCord+(width/16),width,width,null);break;//g.setColor(Color.red);g.drawString("pawn", xCord, yCord);break;
				case -2: g.drawImage(bRook,xCord,yCord,width,width,null);break;//g.setColor(Color.red);g.drawString("rook", xCord, yCord);break;
				case -3: g.drawImage(bKnight,xCord,yCord,width,width,null);break;//g.setColor(Color.red);g.drawString("knight", xCord, yCord);break;
				case -4: g.drawImage(bBishop,xCord,yCord,width,width,null);break;//g.setColor(Color.red);g.drawString("bishop", xCord, yCord);break;
				case -5: g.drawImage(bQueen,xCord,yCord,width,width,null);break;//g.setColor(Color.red);g.drawString("queen", xCord, yCord);break;
				case -6: if(Zboard.blackCheck()) {g.setColor(Color.red);g.fillRect(xCord-5,yCord-5,width+10,width+10);}g.drawImage(bKing,xCord,yCord,width,width,null);break;//g.setColor(Color.red);g.drawString("king", xCord, yCord);break;
				
				default:
				}
			}
		}
	}
	
	public void drawBoardBackground(Graphics g)
	{
		resizeIfNeed();
		//fill in all the white
		g.setColor(white);
		g.fillRect(xCordBack, yCordBack, BackLength, BackLength);
		//fill in all the black
		g.setColor(black);
		for(int xStep = 0; xStep < 8; xStep ++)
		{
			for(int yStep = 0; yStep < 8; yStep ++)
			{
				if(xStep%2 != yStep%2)
				{
					g.fillRect(xCordBack + xStep*(BackLength/8), yCordBack + yStep*(BackLength/8), BackLength/8, BackLength/8);
				}
			}
		}
	}
	
	//this method will be called upon initialization automatically to get proper sizing and queues
	public void resizeIfNeed()
	{
		int realWidth = window.getSize().width - 12;
		//System.out.println(realWidth);
		int realHeight = window.getSize().height - 35;
		//System.out.println(realHeight);
		
		if(realWidth == Qwidth && realHeight == Qheight)
		{
			//good, do nothing
		}
		else
		{
			int limit;
			//first figure out what we are bound by
			if(realWidth>=realHeight)
			{
				limit = realHeight;
			}
			else
			{
				
				limit = realWidth;
			}
			//then calculate the proper size for the board background
			//first calculate the length of the board
			BackLength = limit - 20;
			//makes sure that the length will support 8 squares of equal size
			//is a bit choppy, perhaps will fix later? shouldn't have to, only need resize once
			if(BackLength%8 != 0)
			{
				int remain = BackLength%8;
				if(remain<5)
				{
					BackLength += 8-remain;
				}
				else
				{
					BackLength -= remain;
				}
			}
			//then get its location
			xCordBack = (realWidth - BackLength)/2;
			yCordBack = (realHeight - BackLength)/2;
			//and set the queued dimensions
			Qwidth = realWidth;
			Qheight = realHeight;
		}
	}
	
	public void generateImagesFromSourceFolder()
	{
		wKing = new ImageIcon(getClass().getClassLoader().getResource("White King 2.png")).getImage();
		wQueen = new ImageIcon(getClass().getClassLoader().getResource("White Queen.png")).getImage();
		wRook = new ImageIcon(getClass().getClassLoader().getResource("White Rook.png")).getImage();
		wKnight = new ImageIcon(getClass().getClassLoader().getResource("White Knight.png")).getImage();
		wBishop = new ImageIcon(getClass().getClassLoader().getResource("White Bishop.png")).getImage();
		wPawn = new ImageIcon(getClass().getClassLoader().getResource("test pawn.png")).getImage();
		
		bKing = new ImageIcon(getClass().getClassLoader().getResource("Black King 2.png")).getImage();
		bQueen = new ImageIcon(getClass().getClassLoader().getResource("Black Queen.png")).getImage();
		bRook = new ImageIcon(getClass().getClassLoader().getResource("Black Rook.png")).getImage();
		bKnight = new ImageIcon(getClass().getClassLoader().getResource("Black Knight.png")).getImage();
		bBishop = new ImageIcon(getClass().getClassLoader().getResource("Black Bishop.png")).getImage();
		bPawn = new ImageIcon(getClass().getClassLoader().getResource("black pawn test.png")).getImage();
		
		singlePlayer = new ImageIcon(getClass().getClassLoader().getResource("Single Player.png")).getImage();
		multiPlayer = new ImageIcon(getClass().getClassLoader().getResource("Multiplayer.png")).getImage();
		settings = new ImageIcon(getClass().getClassLoader().getResource("Settings.png")).getImage();
		credits = new ImageIcon(getClass().getClassLoader().getResource("Credits.png")).getImage();
		resume = new ImageIcon(getClass().getClassLoader().getResource("Resume.png")).getImage();
		viewBoard = new ImageIcon(getClass().getClassLoader().getResource("View Board.png")).getImage();
		returnHome = new ImageIcon(getClass().getClassLoader().getResource("Return Home.png")).getImage();
		homeNsave = new ImageIcon(getClass().getClassLoader().getResource("Save and Return Home.png")).getImage();
		homeNdiscard = new ImageIcon(getClass().getClassLoader().getResource("Discard and Return Home.png")).getImage();
		startNew = new ImageIcon(getClass().getClassLoader().getResource("Discard and Begin New Game.png")).getImage();
		back = new ImageIcon(getClass().getClassLoader().getResource("Back_Button.png")).getImage();
		
		homeBackground = new ImageIcon(getClass().getClassLoader().getResource("Background_3.png")).getImage();
		promptBackground = new ImageIcon(getClass().getClassLoader().getResource("Background_4.png")).getImage();
		//boardBackground = new ImageIcon(getClass().getClassLoader().getResource("Background_2.png")).getImage();
		endgameBackground = new ImageIcon(getClass().getClassLoader().getResource("Background_5.png")).getImage();
		
		creditsThemselves = new ImageIcon(getClass().getClassLoader().getResource("Credits_back.png")).getImage();
		whiteVictory = new ImageIcon(getClass().getClassLoader().getResource("White_Victory.png")).getImage();
		blackVictory = new ImageIcon(getClass().getClassLoader().getResource("Black_Victory.png")).getImage();
		stalemate = new ImageIcon(getClass().getClassLoader().getResource("Stalemate.png")).getImage();
	}
	
	@Override
	public void mousePressed(MouseEvent e)
	{
		//first find out where i clicked
		int mouseX = e.getX();
		int mouseY = e.getY();
		
		if(screen == 1)
		{
			mousePressedHomeScreen(mouseX, mouseY);
		}
		else if(screen == 2)
		{
			mousePressedGameBoardMultiplayer(mouseX, mouseY);
		}
		else if(screen == 3)
		{
			mousePressedGameBoardSinglePlayer(mouseX, mouseY);
		}
		else if(screen == 4)
		{
			mousePressedResumeMultiPlayerPrompt(mouseX, mouseY);
		}
		else if(screen == 5)
		{
			mousePressedResumeSinglePlayerPrompt(mouseX, mouseY);
		}
		else if(screen == 6)
		{
			mousePressedReturnHomeSinglePlayerPrompt(mouseX, mouseY);
		}
		else if(screen == 7)
		{
			mousePressedReturnHomeMultiPlayerPrompt(mouseX, mouseY);
		}
		else if(screen == 8)
		{
			mousePressedEndgamePromptS(mouseX, mouseY);
		}
		else if(screen == 9)
		{
			mousePressedEndgamePromptM(mouseX, mouseY);
		}
		else if(screen == 10)
		{
			mousePressedSettings(mouseX, mouseY);
		}
		else if(screen == 11)
		{
			mousePressedCredits(mouseX, mouseY);
		}
	}
	
	public void mousePressedGameBoardSinglePlayer(int mouseX, int mouseY)
	{
		//if we undo after game is over and we still want to keep playing
		if(!Zboard.gameOver)
		{
			viewing = false;
		}
		if(viewing)
		{
			screen = 8;
		}
		else
		{
			int squareX = ((mouseX-((Qwidth-BackLength)/2)-6)/(BackLength/8));
			int squareY = 7-((mouseY-((Qheight-BackLength)/2)-30)/(BackLength/8));
			
			//first check if we are out of bounds of board
			if(mouseX > (((Qwidth-BackLength)/2) + BackLength + 6)
					|| mouseX < (Qwidth-BackLength)/2
					|| mouseY > ((Qheight-BackLength)/2) + BackLength + 30
					|| mouseY < (Qheight-BackLength)/2)
			{
				clickedOOB = true;
				tileClicked = false;
			}
			//if we did not click out of bounds
			if(!clickedOOB)
			{
				// if there is not a tile clicked already
				if(!tileClicked)
				{
					//determine if we have a piece to highlight and if we are on the right turn
					if(Zboard.getTurn() == true && Qpieces[squareX][squareY] > 0)
					{
						xTileClick = squareX;
						yTileClick = squareY;
						tileClicked = true;
					}
				}
				//if there is a tile clicked
				else
				{
					//get all the legal moves for this piece
					int index = 0;
					int[] legals = Zboard.getLegalTiles(xTileClick, yTileClick);
					legalCheck: while(legals[index] != 0 && index<27)
					{
						int nextLegal = legals[index];
						int xPos = 0; int yPos = 0;
						while(nextLegal%3==0){nextLegal/=3;xPos++;}
						while(nextLegal%5==0){nextLegal/=5;yPos++;}
						//System.out.println(squareX + " " + squareY);
						if(squareX == xPos && squareY == yPos)
						{
							Zboard.move(xTileClick, yTileClick, squareX, squareY);
							tileClicked = false;
							break legalCheck;
						}	
						index++;
					}
					//if we click on a different piece we are allowed to move
					//while showing avaliable moves for a different piece
					if(((Zboard.getTurn() == true && Qpieces[squareX][squareY] > 0)
							|| (Zboard.getTurn() == false && Qpieces[squareX][squareY] < 0)) 
							&& (squareX != xTileClick || squareY != yTileClick))
					{
						xTileClick = squareX;
						yTileClick = squareY;
						tileClicked = true;
					}
					//the tile we clicked is not a viable place to move
					else
					{
						tileClicked = false;
					}
				}
			}
			else
			{
				tileClicked = false;
			}
		}
	}
	
	public void mousePressedGameBoardMultiplayer(int mouseX, int mouseY)
	{
		//if we undo after game is over and we still want to keep playing
		if(!Zboard.gameOver)
		{
			viewing = false;
		}
		if(viewing)
		{
			screen = 9;
		}
		else
		{
			//find what corresponding square i clicked
			int squareX;
			int squareY;
			if(!Zboard.getTurn() && flip)
			{
				squareX = 7-((mouseX-((Qwidth-BackLength)/2)-6)/(BackLength/8));
				squareY = ((mouseY-((Qheight-BackLength)/2)-30)/(BackLength/8));
			}
			else
			{
				squareX = ((mouseX-((Qwidth-BackLength)/2)-6)/(BackLength/8));
				squareY = 7-((mouseY-((Qheight-BackLength)/2)-30)/(BackLength/8));
			}
			//first check if we are out of bounds of board
			if(mouseX > (((Qwidth-BackLength)/2) + BackLength + 6)
					|| mouseX < (Qwidth-BackLength)/2
					|| mouseY > ((Qheight-BackLength)/2) + BackLength + 30
					|| mouseY < (Qheight-BackLength)/2)
			{
				clickedOOB = true;
				tileClicked = false;
			}
			//if we did not click out of bounds
			if(!clickedOOB)
			{
				// if there is not a tile clicked already
				if(!tileClicked)
				{
					//determine if we have a piece to highlight and if we are on the right turn
					if((Zboard.getTurn() == true && Qpieces[squareX][squareY] > 0)
							|| (Zboard.getTurn() == false && Qpieces[squareX][squareY] < 0))
					{
						xTileClick = squareX;
						yTileClick = squareY;
						tileClicked = true;
					}
				}
				//if there is a tile clicked
				else
				{
					//get all the legal moves for this piece
					int index = 0;
					int[] legals = Zboard.getLegalTiles(xTileClick, yTileClick);
					legalCheck: while(legals[index] != 0 && index<27)
					{
						int nextLegal = legals[index];
						int xPos = 0; int yPos = 0;
						while(nextLegal%3==0){nextLegal/=3;xPos++;}
						while(nextLegal%5==0){nextLegal/=5;yPos++;}
						//System.out.println(squareX + " " + squareY);
						if(squareX == xPos && squareY == yPos)
						{
							Zboard.move(xTileClick, yTileClick, squareX, squareY);
							tileClicked = false;
							//Zboard.nextTurn();
							//System.out.println("good");
							break legalCheck;
						}	
						index++;
					}
					//if we click on a different piece we are allowed to move
					//while showing avaliable moves for a different piece
					if(((Zboard.getTurn() == true && Qpieces[squareX][squareY] > 0)
							|| (Zboard.getTurn() == false && Qpieces[squareX][squareY] < 0)) 
							&& (squareX != xTileClick || squareY != yTileClick))
					{
						xTileClick = squareX;
						yTileClick = squareY;
						tileClicked = true;
					}
					//the tile we clicked is not a viable place to move
					else
					{
						tileClicked = false;
					}
				}
			}
			else
			{
				tileClicked = false;
			}
		}
	}
	
	public void mousePressedHomeScreen(int x, int y)
	{
		if((x >= xCordBack + BackLength/8 + 6 && x <= xCordBack + BackLength/8 + 3*BackLength/4 + 6) 
				&& (y >= yCordBack + 30 + BackLength/4 && y <= yCordBack + 30+ BackLength/4 + BackLength/8))
		{
			if(doesSaveExistS())
			{
				screen = 5;
			}
			else
			{
				Zboard.loadNewGame();
				screen = 3;
				flip = false;
			}
		}
		else if((x >= xCordBack + BackLength/8 + 6 && x <= xCordBack + BackLength/8 + 3*BackLength/4 + 6) 
				&& (y >= yCordBack + 30 + BackLength/2 && y <= yCordBack + 30 + BackLength/2 + BackLength/8))
		{
			if(doesSaveExistM())
			{
				screen = 4;
			}
			else
			{
				Zboard.loadNewGame();
				screen = 2;
				flip = true;
			}
		}
		else if((x >= xCordBack + BackLength/8 + 6 && x <= xCordBack + BackLength/8 + 5*BackLength/16 + 6)
				&& (y >= yCordBack + 30 + 3*BackLength/4 && y<= yCordBack + 30 + 3*BackLength/4 + BackLength/8))
		{
			screen = 10;
		}
		else if((x >= xCordBack + BackLength/8 + 6 + 7*BackLength/16 && x<= xCordBack + BackLength/8 + 6 + 3*BackLength/4)
				&& (y >= yCordBack + 30 + 3*BackLength/4 && y<= yCordBack + 30 + 3*BackLength/4 + BackLength/8))
		{
			screen = 11;
		}
	}
	
	public void mousePressedResumeMultiPlayerPrompt(int x, int y)
	{
		if((x >= xCordBack + BackLength/8 + 6 && x <= xCordBack + BackLength/8 + BackLength - BackLength/4 + 6)
				&& (y >= yCordBack + BackLength/4 + 30 && y <= yCordBack + BackLength/4 + BackLength/8 + 30))
		{
			loadSavedGameM();
			screen = 2;
			flip = true;
		}
		else if((x >= xCordBack + BackLength/8 + 6 && x <= xCordBack + BackLength/8 + BackLength - BackLength/4 + 6)
				&& (y >= yCordBack + BackLength/2 + 30 && y <= yCordBack + BackLength/2 + BackLength/8 + 30))
		{
			destroySavedGameM();
			Zboard.loadNewGame();
			screen = 2;
			flip = true;
		}
	}
	
	public void mousePressedResumeSinglePlayerPrompt(int x, int y)
	{
		if((x >= xCordBack + BackLength/8 + 6 && x <= xCordBack + BackLength/8 + BackLength - BackLength/4 + 6)
				&& (y >= yCordBack + BackLength/4 + 30 && y <= yCordBack + BackLength/4 + BackLength/8 + 30))
		{
			loadSavedGameS();
			screen = 3;
			flip = false;
		}
		else if((x >= xCordBack + BackLength/8 + 6 && x <= xCordBack + BackLength/8 + BackLength - BackLength/4 + 6)
				&& (y >= yCordBack + BackLength/2 + 30 && y <= yCordBack + BackLength/2 + BackLength/8 + 30))
		{
			destroySavedGameS();
			Zboard.loadNewGame();
			screen = 3;
			flip = false;
		}
	}
	
	public void mousePressedReturnHomeSinglePlayerPrompt(int x, int y)
	{
		if((x >= xCordBack + BackLength/8 + 6 && x<= xCordBack + BackLength/8 + BackLength - BackLength/4 + 6)
				&& (y >= yCordBack + BackLength/8 + 30 && y <= yCordBack + BackLength/8 + BackLength/8 + 30))
		{
			//resume
			screen = 3;
		}
		else if((x >= xCordBack + BackLength/8 + 6 && x<= xCordBack + BackLength/8 + BackLength - BackLength/4 + 6)
				&& (y >= yCordBack + (BackLength/8)*3 + 30 && y <= yCordBack + (BackLength/8)*3 + BackLength/8 + 30))
		{
			//save and return home
			saveGameS();
			screen = 1;
		}
		else if((x >= xCordBack + BackLength/8 + 6 && x<= xCordBack + BackLength/8 + BackLength - BackLength/4 + 6)
				&& (y >= yCordBack + (BackLength/8)*5 + 30 && y <= yCordBack + (BackLength/8)*5 + BackLength/8 + 30))
		{
			//discard and return home
			destroySavedGameS();
			screen = 1;			
		}
	}
	
	public void mousePressedReturnHomeMultiPlayerPrompt(int x, int y)
	{
		if((x >= xCordBack + BackLength/8 + 6 && x<= xCordBack + BackLength/8 + BackLength - BackLength/4 + 6)
				&& (y >= yCordBack + BackLength/8 + 30 && y <= yCordBack + BackLength/8 + BackLength/8 + 30))
		{
			//resume
			screen = 2;
		}
		else if((x >= xCordBack + BackLength/8 + 6 && x<= xCordBack + BackLength/8 + BackLength - BackLength/4 + 6)
				&& (y >= yCordBack + (BackLength/8)*3 + 30 && y <= yCordBack + (BackLength/8)*3 + BackLength/8 + 30))
		{
			//save and return home
			saveGameM();
			screen = 1;
		}
		else if((x >= xCordBack + BackLength/8 + 6 && x<= xCordBack + BackLength/8 + BackLength - BackLength/4 + 6)
				&& (y >= yCordBack + (BackLength/8)*5 + 30 && y <= yCordBack + (BackLength/8)*5 + BackLength/8 + 30))
		{
			//discard and return home
			destroySavedGameM();
			screen = 1;
		}
	}
	
	public void mousePressedEndgamePromptS(int x, int y)
	{
		if((x >= xCordBack + BackLength/8 + 6 && x <= xCordBack + BackLength/8 + BackLength - BackLength/4 + 6)
				&& (y >= yCordBack + BackLength/4 + 30 && y <= yCordBack + BackLength/4 + BackLength/8 + 30))
		{
			screen = 3;
			viewing = true;
		}
		else if((x >= xCordBack + BackLength/8 + 6 && x <= xCordBack + BackLength/8 + BackLength - BackLength/4 + 6)
				&& (y >= yCordBack + BackLength/2 + 30 && y <= yCordBack + BackLength/2 + BackLength/8 + 30))
		{
			destroySavedGameS();
			Zboard.loadNewGame();
			screen = 1;
			viewing = false;
		}
	}
	
	public void mousePressedEndgamePromptM(int x, int y)
	{
		if((x >= xCordBack + BackLength/8 + 6 && x <= xCordBack + BackLength/8 + BackLength - BackLength/4 + 6)
				&& (y >= yCordBack + BackLength/4 + 30 && y <= yCordBack + BackLength/4 + BackLength/8 + 30))
		{
			screen = 2;
			viewing = true;
		}
		else if((x >= xCordBack + BackLength/8 + 6 && x <= xCordBack + BackLength/8 + BackLength - BackLength/4 + 6)
				&& (y >= yCordBack + BackLength/2 + 30 && y <= yCordBack + BackLength/2 + BackLength/8 + 30))
		{
			destroySavedGameM();
			Zboard.loadNewGame();
			screen = 1;
			viewing = false;
		}
	}
	
	public void mousePressedSettings(int x, int y)
	{
		//press of the back button
		if((x >= xCordBack + BackLength/4 + 6 && x <= xCordBack + BackLength/4 + 6 + BackLength/2)
				&& (y >= yCordBack + 3*BackLength/4 + 30 && y <= yCordBack + 3*BackLength/4 + 30 + BackLength/8))
		{
			screen = 1;
		}
	}
	
	public void mousePressedCredits(int x, int y)
	{
		//xCordBack + BackLength/4, yCordBack + 3*BackLength/4, BackLength/2, BackLength/8
		if((x >= xCordBack + BackLength/4 + 6 && x <= xCordBack + BackLength/4 + 6 + BackLength/2)
				&& (y >= yCordBack + 3*BackLength/4 + 30 && y <= yCordBack + 3*BackLength/4 + 30 + BackLength/8))
		{
			screen = 1;
		}
	}
	
	@Override
	public void keyPressed(KeyEvent e)
	{
		int key = e.getKeyCode();
		if(key == 17)
		{
			ctrlPressed = true;
		}
		if(screen == 2 && key == 72)
		{
			if(viewing)
			{
				screen = 9;
			}
			else if(!Zboard.getGameHistory().isEmpty())
			{
				screen = 7;
			}
			else
			{
				screen = 1;
			}
		}
		if(screen == 3 && key == 72)
		{
			if(viewing)
			{
				screen = 8;
			}
			else if(!Zboard.getGameHistory().isEmpty())
			{
				screen = 6;
			}
			else
			{
				screen = 1;
			}
		}
		if((screen == 2 || screen == 3) && key == 90 && undoPressed == false && ctrlPressed)
		{
			undoPressed = true;
		}
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{
		
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
		int key = e.getKeyCode();
		if(undoPressed)
		{
			if(key == 90)
			{
				if(screen == 2)
				{
					Zboard.undo();
				}
				else if(screen == 3)
				{
					Zboard.undo();
				}
				undoPressed = false;
			}
		}
		if(key == 17)
		{
			ctrlPressed = false;
		}
	}
	
	public boolean doesSaveExistS()
	{
		if(sHistory.readLine(1).equals("")) {return false;} return true;
	}
	
	public boolean doesSaveExistM()
	{
		if(mHistory.readLine(1).equals("")) {return false;} return true;
	}
	
	public void loadSavedGameS()
	{
		ArrayList<String> history = new ArrayList<String>();
		int gameLen = sHistory.numLines();
		for(int step = 0; step < gameLen - 1; step++)
		{
			history.add(sHistory.readLine(step + 1));
		}
		Zboard.setHistory(history);
	}
	
	public void loadSavedGameM()
	{
		ArrayList<String> history = new ArrayList<String>();
		int gameLen = mHistory.numLines();
		for(int step = 0; step < gameLen - 1; step++)
		{
			history.add(mHistory.readLine(step + 1));
		}
		Zboard.setHistory(history);
	}
	
	public void saveGameS()
	{
		ArrayList<String> history = Zboard.getGameHistory();
		for(String gameState : history)
		{
			sHistory.writeln(gameState);
		}
	}
	
	public void saveGameM()
	{
		ArrayList<String> history = Zboard.getGameHistory();
		for(String gameState : history)
		{
			mHistory.writeln(gameState);
		}
	}
	
	public void destroySavedGameS()
	{
		sHistory.eraseEntireFile();
	}
	
	public void destroySavedGameM()
	{
		mHistory.eraseEntireFile();
	}
	
	//unused
	public void mouseClicked(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void keyTyped(KeyEvent e) {}
	public void mouseReleased(MouseEvent e) {}
}

package game;

public class CHESS_MAIN
{
	public static void main(String[] args)
	{
		ChessBoard MasterGame = new ChessBoard();
		Chess_RenderingEngine MasterRender = new Chess_RenderingEngine(MasterGame);
		ChessBoardAI Brain = new ChessBoardAI(MasterGame, false);
	}
}
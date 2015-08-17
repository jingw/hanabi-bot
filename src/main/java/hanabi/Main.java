package hanabi;

public class Main {
    public static void main(String[] args) {
        Player[] players = {new CheatingPlayer(), new CheatingPlayer(), new CheatingPlayer()};
        GameState state = new GameState(false, 3, RandomUtil.INSTANCE);
        GameController controller = new GameController(state, players);
        controller.run();
        System.out.println(controller.getState().getScore());
        System.out.println(controller.getState().getTurns());
    }
}

package game.engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import game.engine.dataloader.DataLoader;
import game.engine.exceptions.InvalidMoveException;
import game.engine.exceptions.OutOfEnergyException;
import game.engine.monsters.*;

public class Game {
    
    // ─── ENGINE VARIABLES ─────────────────────────────────────────────────────
    private Board board;
    private ArrayList<Monster> allMonsters;
    private Monster player;
    private Monster opponent;
    private Monster current;
    private int lastRoll = 0;

    public Game() {}

    public Game(Role playerRole) throws IOException {
        this.board = new Board(DataLoader.readCards());
        this.allMonsters = DataLoader.readMonsters();
        
        // 1. Create a dynamic pool of all 8 monsters
        ArrayList<Monster> pool = new ArrayList<>(this.allMonsters);
        Collections.shuffle(pool);

        // 2. Assign Player 1 from the pool and remove them so they can't be on the board
        this.player = pool.stream().filter(m -> m.getRole() == playerRole).findFirst().orElse(null);
        pool.remove(this.player);
        
        // 3. Assign Player 2 from the pool (Opposite role, different type) and remove them
        Role oppRole = (playerRole == Role.SCARER) ? Role.LAUGHER : Role.SCARER;
        this.opponent = pool.stream().filter(m -> m.getRole() == oppRole && !m.getClass().equals(this.player.getClass())).findFirst().orElse(null);
        pool.remove(this.opponent);

        this.current = this.player;

        // 4. The 6 monsters perfectly remaining in the pool act as the stationed cells
        ArrayList<Monster> remainingStationedMonsters = new ArrayList<>(pool);
        Board.setStationedMonsters(remainingStationedMonsters);
        board.initializeBoard(DataLoader.readCells());
    }

    public Board getBoard() { return board; }
    public ArrayList<Monster> getAllMonsters() { return allMonsters; }
    public Monster getPlayer() { return player; }
    public Monster getOpponent() { return opponent; }
    public Monster getCurrent() { return current; }
    public void setCurrent(Monster current) { this.current = current; }
    public int getLastRoll() { return lastRoll; }

    private Monster getCurrentOpponent() {
        return current == player ? opponent : player;
    }

    private int rollDice() {
        Random rand = new Random();
        return rand.nextInt(6) + 1;
    }

    public void usePowerup() throws OutOfEnergyException {
        if (current.getEnergy() < Constants.POWERUP_COST)
            throw new OutOfEnergyException("Not enough energy to use powerup");
        current.executePowerupEffect(getCurrentOpponent());
        current.setEnergy(current.getEnergy() - Constants.POWERUP_COST);
    }

    public void playTurn() throws InvalidMoveException {
        if (current.isFrozen()) {
            System.out.println(current.getName() + " is frozen! Turn skipped.");
            current.setFrozen(false);
            lastRoll = 0; 
            switchTurn();
            return;
        }
        lastRoll = rollDice();
        board.moveMonster(current, lastRoll, getCurrentOpponent());
        switchTurn();
    }

    private void switchTurn() {
        this.setCurrent(getCurrentOpponent());
    }

    private boolean checkWinCondition(Monster monster) {
        return monster.getPosition() == Constants.WINNING_POSITION &&
               monster.getEnergy() >= Constants.WINNING_ENERGY;
    }

    public Monster getWinner() {
        if (checkWinCondition(player)) return player;
        if (checkWinCondition(opponent)) return opponent;
        return null;
    }
}
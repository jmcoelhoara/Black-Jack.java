import java.awt.*; // Contain classes to create user interface and for painting graphics and images
import java.awt.event.*;
import java.util.Random; // Shuffle the cards
import java.util.ArrayList; // Hold the cards in the player's hand
import javax.swing.*;

/*
This project has been done having in mind a regular blackjack game. I will add here the video used for reference.
https://www.youtube.com/watch?v=GMdgjaDdOjI&t=9s
 */

public class BlackJack {
    private class Card { // Card constructor
        String value; // Store the value 1-13
        String type;  // Store the card type Hearts, Spades,...

        Card(String value, String type) {
            this.value = value;
            this.type = type;
        }

        public String toString() {
            return value + "-" + type;
        }

        public int getValue() {
            switch (value) {
                case "A": return 1;
                case "J": return 11;
                case "Q": return 12;
                case "K": return 13;
                default: return Integer.parseInt(value);
            }
        }

        public boolean isAce(){
            return value.equals("A");
        }

        // Get the corresponded image for the card on the player and dealer hand
        public String getImagePath(){
            return "./cards/" + toString() + ".png";
        }
    }

    //Creates the random deck and add inside an arraylist to hold it value
    ArrayList<Card> deck;
    Random random = new Random();

    // Dealer (Computer)
    ArrayList<Card> dealerHand;
    int dealerSum;
    int dealerAceCount;
    int dealerTurnCount; // track number of turns

    // Player
    ArrayList<Card> playerHand;
    int playerSum;
    int playerAceCount;
    int playerTurnCount; // track number of turns

    // Score tracking
    int playerScore = 0;
    int dealerScore = 0;

    // GUI - Window game
    int bordWidth = 700;
    int bordHeight = bordWidth;

    int cardWidth = 110;
    int cardHeight = 154;

    //GUI Frame creation
    JFrame frame = new JFrame("Card Game - Get to 33!");
    JPanel gamePanel = new JPanel(){
        @Override
        public void paintComponent(Graphics g){
            super.paintComponent(g);

            try {
                // Draw dealer's hand
                for(int i = 0; i < dealerHand.size(); i++){
                    Card card = dealerHand.get(i);
                    Image cardImage = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                    g.drawImage(cardImage, 20 + (cardWidth + 5) * i, 20, cardWidth, cardHeight, null);
                }

                // Draw player's hand
                for(int i = 0; i < playerHand.size(); i++){
                    Card card = playerHand.get(i);
                    Image cardImage = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                    g.drawImage(cardImage, 20 + (cardWidth + 5) * i, 320, cardWidth, cardHeight, null );
                }

                // Show result if game is done
                if(!hitButton.isEnabled() && !skipButton.isEnabled()) {
                    String message = getRoundResult();
                    g.setFont(new Font("Arial", Font.PLAIN, 30));
                    g.setColor(Color.white);
                    g.drawString(message, 220, 250);
                    g.setFont(new Font("Arial", Font.PLAIN, 20));
                    g.drawString("Player Wins: " + playerScore + " | Computer Wins: " + dealerScore, 180, 280);
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    };

    //Creates the button on the GUI
    JPanel buttonPanel = new JPanel();
    JButton hitButton = new JButton("HIT");
    JButton stayButton = new JButton("STAY");
    JButton skipButton = new JButton("SKIP"); // New: skip turn
    JButton newRoundButton = new JButton("New Round"); // New: next round

    //Game Starts from here...
    BlackJack(){
        setupGUI();
        startGame();
    }

    //GUI is activated with it parameters
    public void setupGUI(){
        frame.setVisible(true);
        frame.setSize(bordWidth, bordHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        gamePanel.setLayout(new BorderLayout());
        gamePanel.setBackground(new Color(255, 102, 102));
        frame.add(gamePanel, BorderLayout.CENTER);

        // Add buttons
        hitButton.setFocusable(false);
        buttonPanel.add(hitButton);
        stayButton.setFocusable(false);
        buttonPanel.add(stayButton);
        skipButton.setFocusable(false);
        buttonPanel.add(skipButton);
        newRoundButton.setFocusable(false);
        newRoundButton.setEnabled(false); // Only enabled after round ends
        buttonPanel.add(newRoundButton);
        frame.add(buttonPanel, BorderLayout.SOUTH); // Position of the buttons set

        // Hit button logic, acctionListener to catch players decision
        hitButton.addActionListener(e -> {
            if (playerTurnCount < 5) {
                drawCard(playerHand, true);
                playerTurnCount++;
                if (reducePlayerAce() > 33 || playerTurnCount >= 5) endPlayerTurn();
            }
            gamePanel.repaint();
        });

        // Stay button logic (ends player turn early)
        stayButton.addActionListener(e -> {
            endPlayerTurn();
            gamePanel.repaint();
        });

        // Skip button logic (counts as a turn)
        skipButton.addActionListener(e -> {
            playerTurnCount++;
            if (playerTurnCount >= 5) endPlayerTurn();
        });

        // New round
        newRoundButton.addActionListener(e -> {
            startGame();
            gamePanel.repaint();
        });

        gamePanel.repaint();
    }

    /* Set all dealers and players hand to 0 points and give them a shuffle deck
     and start adding their cards to the array list
     */
    public void startGame(){
        buildDeck();
        shuffleDeck();

        dealerHand = new ArrayList<>();
        dealerSum = 0;
        dealerAceCount = 0;
        dealerTurnCount = 0;

        playerHand = new ArrayList<>();
        playerSum = 0;
        playerAceCount = 0;
        playerTurnCount = 0;

        // Enable buttons
        hitButton.setEnabled(true);
        stayButton.setEnabled(true);
        skipButton.setEnabled(true);
        newRoundButton.setEnabled(false);

        System.out.println("New round started.");
    }

    public void drawCard(ArrayList<Card> hand, boolean isPlayer){
        Card card = deck.remove(deck.size()-1);
        hand.add(card);
        if(isPlayer){
            playerSum += card.getValue();
            playerAceCount += card.isAce() ? 1 : 0;
        } else {
            dealerSum += card.getValue();
            dealerAceCount += card.isAce() ? 1 : 0;
        }
    }

    public void endPlayerTurn(){
        hitButton.setEnabled(false);
        stayButton.setEnabled(false);
        skipButton.setEnabled(false);

        // Dealer plays
        while(dealerTurnCount < 5 && reduceDealerAce() < 27) {
            drawCard(dealerHand, false);
            dealerTurnCount++;
        }

        // Determine result and update score
        String result = getRoundResult();
        if(result.contains("Player Wins")) playerScore++;
        else if(result.contains("Computer Wins")) dealerScore++;

        newRoundButton.setEnabled(true);
    }

    public String getRoundResult(){
        dealerSum = reduceDealerAce();
        playerSum = reducePlayerAce();

        System.out.println("Final Player: " + playerSum);
        System.out.println("Final Dealer: " + dealerSum);

        if(playerSum > 33 && dealerSum > 33) return "Both Busted - Draw";
        if(playerSum > 33) return "Computer Wins!";
        if(dealerSum > 33) return "Player Wins!";
        if(playerSum == dealerSum) return "Draw!";
        if(Math.abs(33 - playerSum) < Math.abs(33 - dealerSum)) return "Player Wins!";
        return "Computer Wins!";
    }

    public void buildDeck(){
        deck = new ArrayList<Card>();
        String[] values = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
        String[] types = {"C", "D", "H", "S"};

        // Add the type and value to the card into to the array list
        for (String type : types) {
            for (String value : values) {
                deck.add(new Card(value, type));
            }
        }
    }

    public void shuffleDeck(){
        for(int i = 0; i < deck.size(); i++){
            int j = random.nextInt(deck.size());
            Card temp = deck.get(i);
            deck.set(i, deck.get(j));  
            deck.set(j, temp);
        }
    }

    public int reducePlayerAce(){
        while(playerSum > 33 && playerAceCount > 0){
            playerSum -= 10;
            playerAceCount--;
        }
        return playerSum;
    }

    public int reduceDealerAce(){
        while(dealerSum > 33 && dealerAceCount > 0){
            dealerSum -= 10;
            dealerAceCount--;
        }
        return dealerSum;
    }

    public static void main(String[] args) {
        new BlackJack();
    }
}

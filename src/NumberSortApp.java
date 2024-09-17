import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class NumberSortApp extends JFrame {
    private static final Dimension FIXED_SIZE = new Dimension(80, 25);
    private static final Color COLOR_BLUE = new Color(0, 0, 139);
    private static final Color COLOR_GREEN = new Color(60, 179, 113);
    private static final Color COLOR_WHITE = Color.WHITE;
    private static final int MAX_VALUE_NUMBER = 1000;
    private static final int MAX_ALLOWED_NUMBER = 30;
    private static final int MAX_ROWS = 10;
    private static final int WIDTH_APP = 800;
    private static final int HEIGHT_APP = 400;
    private static final int NUMBER_INSET = 5;

    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private JPanel sortPanel;
    private JTextField numberField;
    private final List<Integer> numbers = new ArrayList<>();
    private boolean isAscending = true;

    public NumberSortApp() {
        setTitle("Number Sort Application");
        setSize(WIDTH_APP, HEIGHT_APP);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        initIntroPanel();
        initSortPanel();

        add(mainPanel);
        setVisible(true);
    }

    private void initIntroPanel() {
        JPanel introPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = createDefaultConstraints();
        gbc.anchor = GridBagConstraints.CENTER;

        addComponent(introPanel, new JLabel("How many numbers to display?"), gbc, 0, 1);
        numberField = new JTextField();
        numberField.setPreferredSize(FIXED_SIZE);
        addComponent(introPanel, numberField, gbc, 0, 2);

        JButton enterButton = createButton("Enter", COLOR_BLUE, COLOR_WHITE, this::handleEnterButton);
        addComponent(introPanel, enterButton, gbc, 0, 3);

        mainPanel.add(introPanel, "Intro");
    }

    private void initSortPanel() {
        sortPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = createDefaultConstraints();

        JButton sortButton = createButton("Sort", COLOR_GREEN, COLOR_WHITE, this::handleSortButton);
        JButton resetButton = createButton("Reset", COLOR_GREEN, COLOR_WHITE, this::handleResetButton);

        addComponent(buttonPanel, sortButton, gbc, 0, 0);
        addComponent(buttonPanel, resetButton, gbc, 0, 1);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(buttonPanel, BorderLayout.NORTH);

        sortPanel.add(new JPanel(), BorderLayout.WEST);
        sortPanel.add(rightPanel, BorderLayout.EAST);

        mainPanel.add(sortPanel, "Sort");
    }

    private JButton createButton(String text, Color bgColor, Color fgColor, ActionListener action) {
        JButton button = new JButton(text);
        button.setPreferredSize(FIXED_SIZE);
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.addActionListener(action);
        return button;
    }

    private void addComponent(JPanel panel, Component comp, GridBagConstraints gbc, int x, int y) {
        gbc.gridx = x;
        gbc.gridy = y;
        panel.add(comp, gbc);
    }

    private void handleEnterButton(ActionEvent e) {
        String value = numberField.getText();
        try {
            int count = Integer.parseInt(value);
            if (count <= 0 || count > MAX_VALUE_NUMBER) {
                showMessage("Enter a number from 1 to 1000 inclusive.");
            } else {
                generateRandomNumbers(count);
                cardLayout.show(mainPanel, "Sort");
            }
        } catch (NumberFormatException ex) {
            showMessage("Invalid number format: " + value + ". Enter a number from 1 to 1000 inclusive.");
            numberField.setText("");
        }
    }

    private void handleSortButton(ActionEvent e) {
        if (!numbers.isEmpty()) {
            quickSort(numbers, 0, numbers.size() - 1);
            updateNumbersDisplay();
            isAscending = !isAscending;
        }
    }

    private void handleResetButton(ActionEvent e) {
        cardLayout.show(mainPanel, "Intro");
    }

    private void generateRandomNumbers(int count) {
        numbers.clear();
        Random rand = new Random();
        for (int i = 0; i < count; i++) {
            numbers.add(rand.nextInt(MAX_VALUE_NUMBER) + 1);
        }
        ensureMinimumAllowedNumber(rand);
        updateNumbersDisplay();
    }

    private void ensureMinimumAllowedNumber(Random rand) {
        if (numbers.stream().noneMatch(n -> n <= MAX_ALLOWED_NUMBER)) {
            numbers.set(rand.nextInt(numbers.size()), rand.nextInt(MAX_ALLOWED_NUMBER) + 1);
        }
    }

    private void updateNumbersDisplay() {
        JPanel buttonPanel = (JPanel) sortPanel.getComponent(0);
        buttonPanel.removeAll();

        int numColumns = (int) Math.ceil(numbers.size() / (double) MAX_ROWS);

        buttonPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = createDefaultConstraints();

        for (int row = 0; row < MAX_ROWS; row++) {
            for (int col = 0; col < numColumns; col++) {
                int index = row + col * MAX_ROWS;
                gbc.gridx = col;
                gbc.gridy = row;

                if (index < numbers.size()) {
                    JButton numberButton = createButton(String.valueOf(numbers.get(index)), COLOR_BLUE, COLOR_WHITE, this::handleNumberButton);
                    buttonPanel.add(numberButton, gbc);
                } else {
                    buttonPanel.add(new JLabel(), gbc);
                }
            }
        }

        buttonPanel.revalidate();
        buttonPanel.repaint();
    }

    private GridBagConstraints createDefaultConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(NUMBER_INSET, NUMBER_INSET, NUMBER_INSET, NUMBER_INSET);
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        return gbc;
    }

    private void handleNumberButton(ActionEvent e) {
        int value = Integer.parseInt(((JButton) e.getSource()).getText());
        if (value <= MAX_ALLOWED_NUMBER) {
            generateRandomNumbers(value);
        } else {
            showMessage("Please select a value smaller or equal to 30.");
        }
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    private void quickSort(List<Integer> list, int low, int high) {
        if (low < high) {
            int pi = partition(list, low, high);
            quickSort(list, low, pi - 1);
            quickSort(list, pi + 1, high);
        }
    }

    private int partition(List<Integer> list, int low, int high) {
        int pivot = list.get(high);
        int i = (low - 1);
        for (int j = low; j < high; j++) {
            if ((isAscending && list.get(j) <= pivot) || (!isAscending && list.get(j) >= pivot)) {
                i++;
                Collections.swap(list, i, j);
            }
        }
        Collections.swap(list, i + 1, high);
        return i + 1;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(NumberSortApp::new);
    }
}

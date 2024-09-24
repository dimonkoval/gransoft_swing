import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.logging.Logger;

public class NumberSortApp2 extends JFrame {
    private static final Color COLOR_RED = Color.RED;
    private static final Color COLOR_YELLOW = new Color(204, 204, 0);
    private static final Color COLOR_BLUE = new Color(0, 0, 139);
    private static final Color COLOR_GREEN = new Color(60, 179, 113);
    private static final Color COLOR_WHITE = Color.WHITE;
    private static final Color COLOR_BLACK = Color.BLACK;
    private static final Dimension FIXED_SIZE = new Dimension(80, 25);
    private static final int MAX_VALUE_NUMBER = 1000;
    private static final int MAX_ALLOWED_NUMBER = 30;
    private static final int MAX_ROWS = 10;
    private static final int WIDTH_APP = 800;
    private static final int HEIGHT_APP = 400;
    private static final int NUMBER_INSET = 5;
    private static final int PAUSE_IN_MILLIS = 400;
    private static final Logger LOGGER = Logger.getLogger(NumberSortApp2.class.getName());

    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private final List<JButton> buttons = new ArrayList<>();
    private final List<Integer> numbers = new ArrayList<>();
    private JTextField numberField;
    private JPanel buttonPanel;
    private boolean isAscending = true;
    private volatile boolean sortingInProgress = false;

    public NumberSortApp2() {
        setTitle("Number Sort Application");
        setSize(WIDTH_APP, HEIGHT_APP);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        initIntroPanel();

        add(mainPanel);
        setVisible(true);
    }

    private void initIntroPanel() {
        JPanel introPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = createDefaultConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        addComponent(introPanel, new JLabel("How many numbers to display?"), gbc, 1);

        numberField = new JTextField();
        numberField.setPreferredSize(FIXED_SIZE);
        addComponent(introPanel, numberField, gbc, 2);

        JButton enterButton = createButton("Enter", COLOR_BLUE, e -> handleEnterButton());
        addComponent(introPanel, enterButton, gbc, 3);

        mainPanel.add(introPanel, "Intro");
    }

    private void initSortPanel() {
        buttonPanel = new JPanel(new GridBagLayout());
        sortingInProgress = true;
        fillButtonPanel();

        JScrollPane scrollPane = new JScrollPane(buttonPanel);
        JPanel sortPanel = new JPanel(new BorderLayout());
        sortPanel.add(scrollPane, BorderLayout.WEST);

        JPanel controlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = createDefaultConstraints();
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.weightx = 1.0;

        JButton resetButton = createButton("Reset", COLOR_GREEN, e -> handleResetButton());
        JButton sortButton = createButton("Sort", COLOR_GREEN, e -> new Thread(this::handleSortButton).start());

        addComponent(controlPanel, sortButton, gbc, 0);
        addComponent(controlPanel, resetButton, gbc, 1);

        gbc.weighty = 1.0;
        controlPanel.add(new JPanel(), gbc);

        sortPanel.add(controlPanel, BorderLayout.EAST);
        mainPanel.add(sortPanel, "Sort");
    }

    private void generateRandomNumbers(int count) {
        Random rand = new Random();
        numbers.clear();
        for (int i = 0; i < count; i++) {
            numbers.add(rand.nextInt(MAX_VALUE_NUMBER) + 1);
        }
        ensureMinimumAllowedNumber(rand);
    }

    private void ensureMinimumAllowedNumber(Random rand) {
        if (numbers.stream().noneMatch(n -> n <= MAX_ALLOWED_NUMBER)) {
            numbers.set(rand.nextInt(numbers.size()), rand.nextInt(MAX_ALLOWED_NUMBER) + 1);
        }
    }

    private void fillButtonPanel() {
        buttons.clear();
        buttonPanel.removeAll();

        GridBagConstraints gbc = createDefaultConstraints();
        int numColumns = (int) Math.ceil(numbers.size() / (double) MAX_ROWS);
        for (int col = 0; col < numColumns; col++) {
            for (int row = 0; row < MAX_ROWS; row++) {
                int index = row + col * MAX_ROWS;
                gbc.gridx = col;
                gbc.gridy = row;

                if (index < numbers.size()) {
                    JButton button = createButton(String.valueOf(numbers.get(index)),
                            COLOR_BLUE, this::handleNumberButton);
                    buttons.add(button);
                    buttonPanel.add(button, gbc);
                } else {
                    buttonPanel.add(new JLabel(), gbc);
                }
            }
        }
        revalidate();
        repaint();
    }

    private void handleSortButton() {
        isAscending = !isAscending;
        buttons.forEach(button -> button.setBackground(COLOR_BLUE));

        sortingInProgress = true;
        quickSort(numbers, 0, numbers.size() - 1);
        if (sortingInProgress) {
            buttons.forEach(button -> button.setBackground(COLOR_GREEN));
        }
        sortingInProgress = false;
    }

    private void quickSort(List<Integer> list, int low, int high) {
        if (!sortingInProgress || low >= high) return;
        int pi = partition(list, low, high);

        if (low < pi - 1) {
            quickSort(list, low, pi - 1);
        } else {
            buttons.get(low).setBackground(COLOR_GREEN);
        }

        if (pi + 1 < high) {
            quickSort(list, pi + 1, high);
        } else {
            buttons.get(high).setBackground(COLOR_GREEN);
        }
    }

    private int partition(List<Integer> list, int low, int high) {
        if (!sortingInProgress) return low;
        int pivot = list.get(high);
        buttons.get(high).setBackground(COLOR_RED);
        pause();

        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (!sortingInProgress) return i;
            buttons.get(j).setBackground(COLOR_BLACK);
            pause();
            if ((isAscending && list.get(j) <= pivot) || (!isAscending && list.get(j) >= pivot)) {
                i++;
                Collections.swap(list, i, j);
                swapButtons(i, j);
                buttons.get(i).setBackground(COLOR_BLUE);
            }
            buttons.get(j).setBackground(COLOR_BLUE);
        }
        Collections.swap(list, i + 1, high);
        swapButtons(i + 1, high);
        buttons.get(i + 1).setBackground(COLOR_GREEN);
        pause();

        return i + 1;
    }

    private void swapButtons(int index1, int index2) {
        if (index1 == index2) return;
        Component component1 = buttons.get(index1);
        Component component2 = buttons.get(index2);

        component1.setBackground(COLOR_YELLOW);
        component2.setBackground(COLOR_YELLOW);
        pause();
        component1.setBackground(COLOR_BLUE);
        component2.setBackground(COLOR_BLUE);

        GridBagLayout layout = (GridBagLayout) buttonPanel.getLayout();

        GridBagConstraints gbc1 = layout.getConstraints(component1);
        GridBagConstraints gbc2 = layout.getConstraints(component2);

        buttonPanel.remove(component1);
        buttonPanel.remove(component2);
        buttonPanel.add(component1, gbc2);
        buttonPanel.add(component2, gbc1);

        Collections.swap(buttons, index1, index2);

        revalidate();
        repaint();
    }

    private void pause() {
        pause(PAUSE_IN_MILLIS);
    }

    private void pause(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            LOGGER.severe("Thread was interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    private void handleNumberButton(ActionEvent e) {
        sortingInProgress = false;
        int value = Integer.parseInt(((JButton) e.getSource()).getText());

        if (value <= MAX_ALLOWED_NUMBER) {
            pause(900);
            prepareAndDisplaySortPanel(value);
        } else {
            showMessage("Please select a value smaller or equal to 30.");
        }
    }

    private void handleEnterButton() {
        String value = numberField.getText();
        try {
            int count = Integer.parseInt(value);
            if (count <= 0 || count > MAX_VALUE_NUMBER) {
                showMessage("Enter a number from 1 to 1000 inclusive.");
            } else {
                prepareAndDisplaySortPanel(count);
            }
        } catch (NumberFormatException ex) {
            showMessage("Invalid number format: " + value + ". Enter a number from 1 to 1000 inclusive.");
            numberField.setText("");
        }
    }

    private void prepareAndDisplaySortPanel(int count) {
        isAscending = true;
        generateRandomNumbers(count);
        initSortPanel();
        cardLayout.show(mainPanel, "Sort");
    }

    private void handleResetButton() {
        sortingInProgress = false;
        isAscending = true;
        numberField.setText("");
        cardLayout.show(mainPanel, "Intro");
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(null, message, "Warning", JOptionPane.ERROR_MESSAGE);
    }

    private GridBagConstraints createDefaultConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(NUMBER_INSET, NUMBER_INSET, NUMBER_INSET, NUMBER_INSET);
        gbc.fill = GridBagConstraints.NONE;
        return gbc;
    }

    private void addComponent(JPanel panel, JComponent component, GridBagConstraints gbc, int y) {
        gbc.gridx = 0;
        gbc.gridy = y;
        panel.add(component, gbc);
    }

    private JButton createButton(String text, Color background, ActionListener actionListener) {
        JButton button = new JButton(text);
        button.setBackground(background);
        button.setForeground(COLOR_WHITE);
        button.setPreferredSize(FIXED_SIZE);
        button.addActionListener(actionListener);
        return button;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(NumberSortApp2::new);
    }
}

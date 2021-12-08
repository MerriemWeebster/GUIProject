package GameOfLife;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class GameOfLife {
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame("Game of Life");
                frame.setSize(1280, 720);
                frame.setResizable(true);
                frame.setMinimumSize(new Dimension(750, 400));
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
                frame.getContentPane().setBackground(Color.GRAY);

                GridModel gridModel = new GridModel();
                GridPanel grid = new GridPanel(gridModel);
                GamePanelView panel = new GamePanelView();
                GamePanelController panelController = new GamePanelController(panel, gridModel, grid);
                
                frame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent we) {
                        panelController.saveState();
                    }
                });
                
                Thread controllerThread = new Thread(panelController);
                controllerThread.start();
                
                Thread panelThread = new Thread(panel);
                panelThread.start();
                
                frame.add(panel);
            }
        });
    }
}

class GamePanelController implements Runnable {

    public static final int GRID_SIZE = 5250, GRID_HEIGHT = 50, GRID_WIDTH = GRID_SIZE / GRID_HEIGHT;

    private GamePanelView panel;
    private GridModel gridModel;
    private GridPanel grid;
    
    private int currentGeneration = 0, currentSpeed = 250, gridHeight, gridSize, blockSize;
    private boolean init = false;
    private String[] patterns = {"Clear", "Block", "Tub", "Boat", "Snake", "Ship", "Aircraft Carrier", "Beehive", "Barge",
            "Python", "Long Boat", "Eater, Fishhook", "Loaf", "Cloverleaf", "Glider"},
            speeds = {"Slow", "Normal", "Fast"}, sizes = {"Small", "Medium", "Big"};
    private Preferences prefs;
    private Timer generationTimer;

	GamePanelController(GamePanelView panel, GridModel gridModel, GridPanel grid)
	{
		this.panel = panel;
		this.gridModel = gridModel;
		this.grid = grid;
		
        prefs = Preferences.userRoot().node(this.getClass().getName() + " - GUI Game");
        
        
        //TODO: proper changes to cells
        LifeCell[] cells = new LifeCell[GRID_SIZE];

        for (int i = 0; i < GRID_SIZE; i++) cells[i] = new LifeCell(i, cells);
        gridModel.setAllCells(cells);

        try {
            if (!Preferences.userRoot().nodeExists(this.getClass().getName() + " - GUI Game")) {
                prefs.putInt("speed", 1);
                prefs.putInt("pattern", 0);
                prefs.putInt("zoom", 1);
                prefs.putBoolean("restore", false);
                prefs.putByteArray("grid", getCellsByteArray());
            }
        } catch (BackingStoreException e2) {
            e2.printStackTrace();
        }
        
        panel.setPatternList(patterns);
        panel.setSizeList(sizes);
        panel.setSpeedList(speeds);
        
        panel.initGridPanel(grid);

        panel.addSaveButtonListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prefs.putInt("speed", panel.getSelectedPrefSpeed());
                prefs.putInt("pattern", panel.getSelectedPrefPattern());
                prefs.putInt("zoom", panel.getSelectedPrefSize());
                prefs.putBoolean("restore", panel.getRestorePrefCheck());
                panel.setPrefsDialogVisible(false);
            }
        });
        
        generationTimer = new Timer(currentSpeed, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentGeneration++;
                updateGame();
        }});
        
        panel.addNextButtonListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentGeneration++;
                updateGame();
            }});

        panel.addStartButtonListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!generationTimer.isRunning()) {
                    generationTimer.start();
                    panel.setNextButtonEnabled(false);
                    panel.setStartButtonText("Stop");
                } else {
                    generationTimer.stop();
                    panel.setNextButtonEnabled(true);
                    panel.setStartButtonText("Start");
                }
            }});

        //pattern menu listener
        panel.addPatternListListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generationTimer.stop();
                panel.setNextButtonEnabled(true);
                panel.setStartButtonText("Start");
                currentGeneration = 0;
                LifeCell[] cells = gridModel.getAllCells();
                
                for (LifeCell cell : cells) cell.setAlive(false);

                int midX = gridSize / 2, midY = gridHeight / 2;
                int startingGrid = midX + midY * gridSize;

                switch (panel.getPatternListSelectedString()) {
                    case "Block" -> {
                        cells[startingGrid].setAlive(true);
                        cells[startingGrid + 1].setAlive(true);
                        cells[startingGrid + gridSize].setAlive(true);
                        cells[startingGrid + gridSize + 1].setAlive(true);
                    }
                    case "Tub" -> {
                        cells[startingGrid + 1].setAlive(true);
                        cells[startingGrid - 1].setAlive(true);
                        cells[startingGrid - gridSize].setAlive(true);
                        cells[startingGrid + gridSize].setAlive(true);
                    }
                    case "Boat" -> {
                        cells[startingGrid + 1].setAlive(true);
                        cells[startingGrid - 1].setAlive(true);
                        cells[startingGrid - gridSize].setAlive(true);
                        cells[startingGrid + gridSize].setAlive(true);
                        cells[startingGrid + gridSize + 1].setAlive(true);
                    }
                    case "Snake" -> {
                        cells[startingGrid - 1].setAlive(true);
                        cells[startingGrid + 1].setAlive(true);
                        cells[startingGrid + 2].setAlive(true);
                        cells[startingGrid + gridSize + 2].setAlive(true);
                        cells[startingGrid + gridSize].setAlive(true);
                        cells[startingGrid + gridSize - 1].setAlive(true);
                    }
                    case "Ship" -> {
                        cells[startingGrid - 1].setAlive(true);
                        cells[startingGrid + 1].setAlive(true);
                        cells[startingGrid + gridSize].setAlive(true);
                        cells[startingGrid + gridSize + 1].setAlive(true);
                        cells[startingGrid - gridSize].setAlive(true);
                        cells[startingGrid - gridSize - 1].setAlive(true);
                    }
                    case "Aircraft Carrier" -> {
                        cells[startingGrid - 1].setAlive(true);
                        cells[startingGrid + 2].setAlive(true);
                        cells[startingGrid + gridSize + 2].setAlive(true);
                        cells[startingGrid + gridSize + 1].setAlive(true);
                        cells[startingGrid - gridSize].setAlive(true);
                        cells[startingGrid - gridSize - 1].setAlive(true);
                    }
                    case "Beehive" -> {
                        cells[startingGrid - 1].setAlive(true);
                        cells[startingGrid + 2].setAlive(true);
                        cells[startingGrid + gridSize].setAlive(true);
                        cells[startingGrid + gridSize + 1].setAlive(true);
                        cells[startingGrid - gridSize].setAlive(true);
                        cells[startingGrid - gridSize + 1].setAlive(true);
                    }
                    case "Barge" -> {
                        cells[startingGrid].setAlive(true);
                        cells[startingGrid + 2].setAlive(true);
                        cells[startingGrid + gridSize + 1].setAlive(true);
                        cells[startingGrid - gridSize - 1].setAlive(true);
                        cells[startingGrid - gridSize + 1].setAlive(true);
                        cells[startingGrid - gridSize * 2].setAlive(true);
                    }
                    case "Python" -> {
                        cells[startingGrid].setAlive(true);
                        cells[startingGrid - 2].setAlive(true);
                        cells[startingGrid + 2].setAlive(true);
                        cells[startingGrid + gridSize - 1].setAlive(true);
                        cells[startingGrid + gridSize - 2].setAlive(true);
                        cells[startingGrid - gridSize + 1].setAlive(true);
                        cells[startingGrid - gridSize + 2].setAlive(true);
                    }
                    case "Long Boat" -> {
                        cells[startingGrid].setAlive(true);
                        cells[startingGrid + 2].setAlive(true);
                        cells[startingGrid + gridSize + 1].setAlive(true);
                        cells[startingGrid + gridSize + 2].setAlive(true);
                        cells[startingGrid - gridSize - 1].setAlive(true);
                        cells[startingGrid - gridSize + 1].setAlive(true);
                        cells[startingGrid - gridSize * 2].setAlive(true);
                    }
                    case "Eater, Fishhook" -> {
                        cells[startingGrid + 1].setAlive(true);
                        cells[startingGrid + gridSize + 1].setAlive(true);
                        cells[startingGrid + gridSize + 2].setAlive(true);
                        cells[startingGrid - gridSize - 1].setAlive(true);
                        cells[startingGrid - gridSize + 1].setAlive(true);
                        cells[startingGrid - gridSize * 2].setAlive(true);
                        cells[startingGrid - gridSize * 2 - 1].setAlive(true);
                    }
                    case "Loaf" -> {
                        cells[startingGrid - 1].setAlive(true);
                        cells[startingGrid + 2].setAlive(true);
                        cells[startingGrid + gridSize].setAlive(true);
                        cells[startingGrid + gridSize + 1].setAlive(true);
                        cells[startingGrid - gridSize - 1].setAlive(true);
                        cells[startingGrid - gridSize + 1].setAlive(true);
                        cells[startingGrid - gridSize * 2].setAlive(true);
                    }
                    case "Cloverleaf" -> {
                        cells[startingGrid + gridSize].setAlive(true);
                        cells[startingGrid + gridSize + 2].setAlive(true);
                        cells[startingGrid + gridSize + 3].setAlive(true);
                        cells[startingGrid + gridSize - 2].setAlive(true);
                        cells[startingGrid + gridSize - 3].setAlive(true);
                        cells[startingGrid + gridSize * 2 + 2].setAlive(true);
                        cells[startingGrid + gridSize * 2 + 4].setAlive(true);
                        cells[startingGrid + gridSize * 2 - 2].setAlive(true);
                        cells[startingGrid + gridSize * 2 - 4].setAlive(true);
                        cells[startingGrid + gridSize * 3].setAlive(true);
                        cells[startingGrid + gridSize * 3 + 4].setAlive(true);
                        cells[startingGrid + gridSize * 3 - 4].setAlive(true);
                        cells[startingGrid + gridSize * 4 - 1].setAlive(true);
                        cells[startingGrid + gridSize * 4 - 2].setAlive(true);
                        cells[startingGrid + gridSize * 4 - 3].setAlive(true);
                        cells[startingGrid + gridSize * 4 + 1].setAlive(true);
                        cells[startingGrid + gridSize * 4 + 2].setAlive(true);
                        cells[startingGrid + gridSize * 4 + 3].setAlive(true);
                        cells[startingGrid + gridSize * 5 - 1].setAlive(true);
                        cells[startingGrid + gridSize * 5 + 1].setAlive(true);
                        cells[startingGrid - gridSize].setAlive(true);
                        cells[startingGrid - gridSize + 2].setAlive(true);
                        cells[startingGrid - gridSize + 3].setAlive(true);
                        cells[startingGrid - gridSize - 2].setAlive(true);
                        cells[startingGrid - gridSize - 3].setAlive(true);
                        cells[startingGrid - gridSize * 2 + 2].setAlive(true);
                        cells[startingGrid - gridSize * 2 + 4].setAlive(true);
                        cells[startingGrid - gridSize * 2 - 2].setAlive(true);
                        cells[startingGrid - gridSize * 2 - 4].setAlive(true);
                        cells[startingGrid - gridSize * 3].setAlive(true);
                        cells[startingGrid - gridSize * 3 + 4].setAlive(true);
                        cells[startingGrid - gridSize * 3 - 4].setAlive(true);
                        cells[startingGrid - gridSize * 4 - 1].setAlive(true);
                        cells[startingGrid - gridSize * 4 - 2].setAlive(true);
                        cells[startingGrid - gridSize * 4 - 3].setAlive(true);
                        cells[startingGrid - gridSize * 4 + 1].setAlive(true);
                        cells[startingGrid - gridSize * 4 + 2].setAlive(true);
                        cells[startingGrid - gridSize * 4 + 3].setAlive(true);
                        cells[startingGrid - gridSize * 5 - 1].setAlive(true);
                        cells[startingGrid - gridSize * 5 + 1].setAlive(true);
                    }
                    case "Glider" -> {
                        cells[startingGrid - gridSize].setAlive(true);
                        cells[startingGrid + 1].setAlive(true);
                        cells[startingGrid + gridSize].setAlive(true);
                        cells[startingGrid + gridSize + 1].setAlive(true);
                        cells[startingGrid + gridSize - 1].setAlive(true);
                    }
                }
            }
        });

        //speed menu thingy listener
        panel.addSpeedListListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switch (panel.getSelectedSpeed()) {
                    case 0 -> currentSpeed = 500;
                    case 1 -> currentSpeed = 250;
                    case 2 -> currentSpeed = 100;
                }

                generationTimer.setDelay(currentSpeed);
                if (generationTimer.isRunning()) generationTimer.restart();
            }
        });

        //size menu list
        panel.addSizeListListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	Point gridOffset = grid.getGridOffset();
                if (panel.getSelectedSize() == 0) gridOffset = new Point();
                updateSize();
                if (gridOffset.x >= (gridSize * blockSize) / 1.5)
                    gridOffset.setLocation((gridSize * blockSize) / 2, gridOffset.y);
                if (gridOffset.x <= -(gridSize * blockSize) / 1.5)
                    gridOffset.setLocation(-(gridSize * blockSize) / 2, gridOffset.y);

                if (gridOffset.y >= (gridHeight * blockSize) / 1.5)
                    gridOffset.setLocation(gridOffset.x, (gridHeight * blockSize) / 2);
                if (gridOffset.y <= -(gridHeight * blockSize) / 1.5)
                    gridOffset.setLocation(gridOffset.x, -(gridHeight * blockSize) / 2);
                
                grid.setGridOffset(gridOffset);
                panel.repaint();
            }});
        
        //grid listener editing the cells
        grid.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                //some computers do not have a proper ID for the left click, so we use button1 or NOBUTTON
                if (panel.isEditModeEnabled() && !generationTimer.isRunning() && (e.getButton() == MouseEvent.BUTTON1 || e.getButton() == MouseEvent.NOBUTTON)) {
                    int offsetX = (grid.getWidth() / 2) - ((gridSize * blockSize) / 2) + grid.getGridOffset().x,
                            offsetY = (grid.getHeight() / 2) - ((gridHeight * blockSize) / 2) + grid.getGridOffset().y;

                    int selectedIndex = -1;

                    for (int i = 0; i < gridSize; i++) {
                        for (int j = 0; j < gridHeight; j++) {
                            int index = i + j * GRID_WIDTH;
                            int posX = i * blockSize + offsetX, posY = j * blockSize + offsetY;

                            if (e.getPoint().getX() >= posX && e.getPoint().getX() <= posX + blockSize - 3
                                    && e.getPoint().getY() >= posY && e.getPoint().getY() <= posY + blockSize - 3) {
                                selectedIndex = index;
                                break;
                            }
                        }

                        if (selectedIndex > -1) break;
                    }

                    if (selectedIndex > -1) {
                        gridModel.getCell(selectedIndex).setAlive(!gridModel.getCell(selectedIndex).isAlive());
                    }
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    JPopupMenu rightClickMenu = new JPopupMenu();
                    JMenuItem save = new JMenuItem("Save"), open = new JMenuItem("Open"), pref = new JMenuItem("Preferences");

                    save.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            GridConfiguration gridConfig = new GridConfiguration(gridModel.getAllCells(), currentGeneration, currentSpeed,
                                    panel.getSelectedSize(), panel.getSelectedPattern(), panel.isEditModeEnabled(), grid.getGridOffset());

                            FileDialog fileDialog = new FileDialog(panel.getTopFrame(), "Save Grid Configuration", FileDialog.SAVE);

                            //it is text based, but we specified it with a .life type so that it only works with our program
                            fileDialog.setFile("*.life");
                            fileDialog.setVisible(true);

                            if (fileDialog.getFile() == null) return;
                            if (!fileDialog.getFile().endsWith(".life"))
                                fileDialog.setFile(fileDialog.getFile() + ".life");

                            try {
                                FileOutputStream fileOutputStream = new FileOutputStream(fileDialog.getDirectory() + "/" + fileDialog.getFile());
                                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                                objectOutputStream.writeObject(gridConfig);
                                objectOutputStream.flush();
                                objectOutputStream.close();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }});

                    //listener for opening a .life file
                    open.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            FileDialog fileDialog = new FileDialog(panel.getTopFrame(), "Load Grid Configuration", FileDialog.LOAD);
                            fileDialog.setFile("*.life");
                            fileDialog.setVisible(true);

                            if (fileDialog.getFile() == null) return;
                            if (!fileDialog.getFile().endsWith(".life")) {
                                JOptionPane.showMessageDialog(panel.getTopFrame(), "Please select a file with the extension \".life\".");
                                return;
                            }

                            GridConfiguration gridConfig = null;
                            try {
                                FileInputStream fileInputStream = new FileInputStream(fileDialog.getDirectory() + "/" + fileDialog.getFile());
                                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                                gridConfig = (GridConfiguration) objectInputStream.readObject();
                                objectInputStream.close();
                            } catch (ClassNotFoundException | IOException e1) {
                                e1.printStackTrace();
                            }

                            if (gridConfig != null) {
                                panel.selectPattern(gridConfig.getStartingPatter());
                                gridModel.setAllCells(gridConfig.getCells());
                                currentGeneration = gridConfig.getGeneration();
                                currentSpeed = gridConfig.getSpeed();

                                int speedIndex = 0;
                                if (currentSpeed == 250) speedIndex = 1;
                                else if (currentSpeed == 100) speedIndex = 2;

                                panel.selectSpeed(speedIndex);
                                panel.selectSize(gridConfig.getSize());
                                panel.setEditMode(gridConfig.getEditMode());
                                grid.setGridOffset(gridConfig.getGridOffset());
                            }
                        }});

                    pref.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            panel.setRestorePrefCheck(prefs.getBoolean("restore", false));
                            panel.selectPrefPattern(prefs.getInt("pattern", 0));
                            panel.selectPrefSpeed(prefs.getInt("speed", 1));
                            panel.selectPrefSize(prefs.getInt("zoom", 1));
                            panel.setPrefsDialogVisible(true);
                        }});

                    rightClickMenu.add(save);
                    rightClickMenu.add(open);
                    rightClickMenu.add(pref);

                    rightClickMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            //NOBUTTON is used as some laptop trackpads have the button ID 0
            //getting the point of the initial position before dragging
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 || e.getButton() == MouseEvent.NOBUTTON)
                    grid.setMouseDragPoint(e.getPoint());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 || e.getButton() == MouseEvent.NOBUTTON)
                    panel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });

        //listener for dragging to look around
        grid.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1 && e.getButton() != MouseEvent.NOBUTTON) return;

                panel.setCursor(new Cursor(Cursor.HAND_CURSOR));

                Point currentPoint = e.getPoint(),
                        currentOffset = new Point(currentPoint.x - grid.getMouseDragPoint().x, currentPoint.y - grid.getMouseDragPoint().y);

                if (grid.getGridOffset().x >= (gridSize * blockSize) / 2 && currentOffset.x > 0)
                    currentOffset.setLocation(0, currentOffset.y);
                if (grid.getGridOffset().x <= -(gridSize * blockSize) / 2 && currentOffset.x < 0)
                    currentOffset.setLocation(0, currentOffset.y);

                if (grid.getGridOffset().y >= (gridHeight * blockSize) / 2 && currentOffset.y > 0)
                    currentOffset.setLocation(currentOffset.x, 0);
                if (grid.getGridOffset().y <= -(gridHeight * blockSize) / 2 && currentOffset.y < 0)
                    currentOffset.setLocation(currentOffset.x, 0);

                grid.setGridOffset(new Point(grid.getGridOffset().x + currentOffset.x, grid.getGridOffset().y + currentOffset.y));
                grid.setMouseDragPoint(e.getPoint());
            }

            @Override
            public void mouseMoved(MouseEvent e) {
            }

        });        
	}

	  public void initData() {
	        panel.selectPattern(prefs.getInt("pattern", 0));
	        panel.selectSize(prefs.getInt("zoom", 1));
	        panel.selectSpeed(prefs.getInt("speed", 1));

	        if (prefs.getBoolean("restore", false)) {
	            boolean[] gridPref = getGridPref();
	            for (int i = 0; i < GRID_SIZE; i++) {
	                gridModel.getAllCells()[i].setAlive(gridPref[i]);
	            }
	        }
	    }

	    public void updateGame() {
	        //we use 2 for loops so that the calculation is done before any updates are made
	        for (LifeCell cell : gridModel.getAllCells()) cell.updateCell();
	        for (LifeCell cell : gridModel.getAllCells()) cell.updateAliveState();
	        panel.repaint();
	    }

	    public void saveState() {
	        prefs.putByteArray("grid", getCellsByteArray());
	    }

	    public void updateSize() {
	        gridHeight = GRID_HEIGHT;
	        gridSize = GRID_WIDTH;
	        blockSize = grid.getHeight() / gridHeight;

	        if (blockSize * gridSize > grid.getWidth()) blockSize = grid.getWidth() / gridSize;

	        if (panel.getSelectedSize() == 1) {
	            blockSize *= 2;
	        }
	        if (panel.getSelectedSize() == 2) {
	            blockSize *= 4;
	        }
	        if (!init) {
	            initData();
	            init = true;
	        }
	    }
	
	  //for restore function
    public byte[] getCellsByteArray() {
        boolean[] cellGrid = new boolean[gridModel.getAllCells().length];

        for (int i = 0; i < gridModel.getAllCells().length; i++) {
            cellGrid[i] = gridModel.getAllCells()[i].isAlive();
        }

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objStream = new ObjectOutputStream(byteStream);
            objStream.writeObject(cellGrid);
            objStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return byteStream.toByteArray();
    }

    public boolean[] getGridPref() {
        boolean[] gridData = new boolean[GRID_SIZE];
        byte[] data = prefs.getByteArray("grid", null);
        if (data != null) {
            ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
            try {
                ObjectInputStream objStream = new ObjectInputStream(byteStream);
                gridData = (boolean[]) objStream.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        return gridData;
    }

    @Override
    public void run()
    {
    	while(true)
    	{
    		updateSize();
	        panel.setGenerationText("Generation: " + currentGeneration);
	        grid.updateGridDetails(gridHeight, gridSize, blockSize);
    	}
    }
	
}

@SuppressWarnings("serial")
class GamePanelView extends JPanel implements Runnable {

	private BorderLayout borderLayout;
    private FlowLayout flowLayout;
    private JPanel controls, prefsPanel;
    private JButton nextButton, startButton, saveButton;
    private JLabel generationLabel;
    private JComboBox<String> patternList, speedList, sizeList;
    private JCheckBox editCheck;
    private JDialog prefsDialog;
    private JCheckBox restorePrefCheck;
    private JComboBox<String> patternPrefList, speedPrefList, sizePrefList;

    public void initGridPanel(GridPanel grid) {

        prefsDialog = new JDialog(getTopFrame(), "Preferences", true);
        prefsDialog.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        prefsPanel = new JPanel();
        prefsPanel.setLayout(new GridLayout(5, 2));

        restorePrefCheck = new JCheckBox("Restore Grid on Start");

        prefsPanel.add(new JLabel("Default Pattern:"));
        prefsPanel.add(patternPrefList);
        prefsPanel.add(new JLabel("Default Speed:"));
        prefsPanel.add(speedPrefList);
        prefsPanel.add(new JLabel("Default Zoom Level:"));
        prefsPanel.add(sizePrefList);
        prefsPanel.add(restorePrefCheck);
        prefsPanel.add(Box.createRigidArea(new Dimension(0, 0)));
        prefsPanel.add(Box.createRigidArea(new Dimension(0, 0)));
        saveButton = new JButton("Save");

        prefsPanel.add(saveButton);
        prefsPanel.setBorder(new EmptyBorder(20, 20, 5, 20));
        prefsDialog.add(prefsPanel);
        prefsDialog.pack();
        prefsDialog.setResizable(false);
       

        borderLayout = new BorderLayout();
        flowLayout = new FlowLayout();
        nextButton = new JButton("Next");
        startButton = new JButton("Start");
        generationLabel = new JLabel("Generation: 0");

        editCheck = new JCheckBox("Edit Mode");

        this.setLayout(borderLayout);

        controls = new JPanel();
        controls.setLayout(flowLayout);
        controls.add(editCheck);
        controls.add(patternList);
        controls.add(nextButton);
        controls.add(startButton);
        controls.add(speedList);
        controls.add(sizeList);
        controls.add(generationLabel);
        controls.setBackground(Color.GRAY);
        
        this.add(grid, BorderLayout.CENTER);
        this.add(controls, BorderLayout.PAGE_END);
    }
        
    public void setPatternList(String[] patterns) 
    { 
    	this.patternList = new JComboBox<String>(patterns); 
    	this.patternPrefList = new JComboBox<String>(patterns);
    }
    public void setSpeedList(String[] speeds) 
    { 
    	this.speedList = new JComboBox<String>(speeds);
    	this.speedPrefList = new JComboBox<String>(speeds);
    }
    public void setSizeList(String[] sizes) 
    { 
    	this.sizeList = new JComboBox<String>(sizes);
    	this.sizePrefList = new JComboBox<String>(sizes);
    }
    
    public void selectPattern(int index) { this.patternList.setSelectedIndex(index); }
    public void selectSpeed(int index) { this.speedList.setSelectedIndex(index); }
    public void selectSize(int index) { this.sizeList.setSelectedIndex(index); }
    public void selectPrefPattern(int index) { this.patternPrefList.setSelectedIndex(index); }
    public void selectPrefSpeed(int index) { this.speedPrefList.setSelectedIndex(index); }
    public void selectPrefSize(int index) { this.sizePrefList.setSelectedIndex(index); }
    public void setRestorePrefCheck(boolean selected) { this.restorePrefCheck.setSelected(selected); }
    public void setGenerationText(String text) { this.generationLabel.setText(text); }    
    public void setPrefsDialogVisible(boolean visible) { this.prefsDialog.setVisible(visible); }    
    public void addSaveButtonListener(ActionListener listener) { this.saveButton.addActionListener(listener); }
    public void addNextButtonListener(ActionListener listener) { this.nextButton.addActionListener(listener); }
    public void addStartButtonListener(ActionListener listener) { this.startButton.addActionListener(listener); }
    public void addSizeListListener(ActionListener listener) { this.sizeList.addActionListener(listener); }
    public void addSpeedListListener(ActionListener listener) { this.speedList.addActionListener(listener); }
    public void addPatternListListener(ActionListener listener) { this.patternList.addActionListener(listener); }  
    public void setNextButtonEnabled(boolean enabled) { this.nextButton.setEnabled(enabled); }
    public void setEditMode(boolean enabled) { this.editCheck.setSelected(enabled); }
    public void setStartButtonText(String text) { this.startButton.setText(text); }    

    public int getSelectedPattern() { return this.patternList.getSelectedIndex(); }
    public int getSelectedSpeed() { return this.speedList.getSelectedIndex(); }
    public int getSelectedSize() { return this.sizeList.getSelectedIndex(); }
    public int getSelectedPrefPattern() { return this.patternPrefList.getSelectedIndex(); }
    public int getSelectedPrefSpeed() { return this.speedPrefList.getSelectedIndex(); }
    public int getSelectedPrefSize() { return this.sizePrefList.getSelectedIndex(); }
    
    public boolean getRestorePrefCheck() { return this.restorePrefCheck.isSelected(); }
    public boolean isEditModeEnabled() { return this.editCheck.isSelected(); }
    public String getPatternListSelectedString() { return this.patternList.getSelectedItem().toString(); }
    

    public JFrame getTopFrame() {
        return (JFrame) SwingUtilities.getWindowAncestor(this);
    }

	@Override
	public void run()
	{
		while(true)
		{
	        repaint();
	        try
			{
				Thread.sleep(10);
			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}

class LifeCell implements Serializable {
    private static final long serialVersionUID = 1L;

    private int xPos, yPos;
    private boolean alive = false, futureAlive = false;
    private LifeCell[] cells;


    LifeCell(int currentIndex, LifeCell[] cells) {
        this.xPos = currentIndex % GamePanelController.GRID_WIDTH;
        this.yPos = currentIndex / GamePanelController.GRID_WIDTH;
        this.cells = cells;
    }

    public int getIndex(int x, int y) {
        return x + y * GamePanelController.GRID_WIDTH;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = this.futureAlive = alive;
    }

    public void updateAliveState() {
        this.alive = this.futureAlive;
    }

    public void updateCell() {
        int neighbours = 0;

        if (xPos < GamePanelController.GRID_WIDTH - 1) {
            if (cells[getIndex(xPos + 1, yPos)].isAlive()) neighbours++;
        }

        if (xPos > 0) {
            if (cells[getIndex(xPos - 1, yPos)].isAlive()) neighbours++;
        }

        if (yPos < GamePanelController.GRID_HEIGHT - 1) {
            if (cells[getIndex(xPos, yPos + 1)].isAlive()) neighbours++;
        }

        if (yPos > 0) {
            if (cells[getIndex(xPos, yPos - 1)].isAlive()) neighbours++;
        }

        if (yPos > 0 && xPos > 0) {
            if (cells[getIndex(xPos - 1, yPos - 1)].isAlive()) neighbours++;
        }

        if (yPos < GamePanelController.GRID_HEIGHT - 1 && xPos < GamePanelController.GRID_WIDTH - 1) {
            if (cells[getIndex(xPos + 1, yPos + 1)].isAlive()) neighbours++;
        }

        if (yPos > 0 && xPos < GamePanelController.GRID_WIDTH - 1) {
            if (cells[getIndex(xPos + 1, yPos - 1)].isAlive()) neighbours++;
        }

        if (yPos < GamePanelController.GRID_HEIGHT - 1 && xPos > 0) {
            if (cells[getIndex(xPos - 1, yPos + 1)].isAlive()) neighbours++;
        }

        if (alive) {
            futureAlive = neighbours == 2 || neighbours == 3;
        } else {
            futureAlive = neighbours == 3;
        }
    }
}

class GridConfiguration implements Serializable {
    private static final long serialVersionUID = 1L;

    private int currentGeneration, currentSpeed, currentSize, startingPattern;
    private boolean editMode;
    private Point gridOffset;
    private LifeCell[] cells;


    GridConfiguration(LifeCell[] cells, int currentGeneration, int currentSpeed, int currentSize, int startingPattern,
                      boolean editMode, Point gridOffset) {
        this.cells = cells;
        this.currentGeneration = currentGeneration;
        this.currentSpeed = currentSpeed;
        this.currentSize = currentSize;
        this.startingPattern = startingPattern;
        this.editMode = editMode;
        this.gridOffset = gridOffset;
    }

    public int getGeneration() {
        return currentGeneration;
    }

    public int getSpeed() {
        return currentSpeed;
    }

    public int getSize() {
        return currentSize;
    }

    public int getStartingPatter() {
        return startingPattern;
    }

    public boolean getEditMode() {
        return editMode;
    }

    public Point getGridOffset() {
        return gridOffset;
    }

    public LifeCell[] getCells() {
        return cells;
    }
}

class GridModel
{
	private LifeCell[] cells;
	
	public LifeCell getCell(int index) { return cells[index]; }
	public LifeCell[] getAllCells() { return cells; }
	public void setCell(int index, LifeCell cell) { cells[index] = cell; }
	public void setAllCells(LifeCell[] cells) { this.cells = cells; }
}

@SuppressWarnings("serial")
class GridPanel extends JPanel
{
	private int gridSize = 0, gridHeight = 0, blockSize = 0;
    private Point mouseDragPoint, gridOffset = new Point(0, 0);
    private GridModel gridModel;
    
    GridPanel(GridModel gridModel)
    {
    	this.gridModel = gridModel;
        this.setBackground(Color.GRAY);
    }

	@Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
                
        Graphics2D g2D = (Graphics2D) g;

        int offsetX = (this.getWidth() / 2) - ((gridSize * blockSize) / 2) + gridOffset.x,
                offsetY = (this.getHeight() / 2) - ((gridHeight * blockSize) / 2) + gridOffset.y;

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridHeight; j++) {
                int index = i + j * GamePanelController.GRID_WIDTH;
                g2D.setColor(Color.DARK_GRAY);

                if (gridModel.getCell(index).isAlive()) {
                    g2D.setColor(Color.YELLOW);
                }

                int posX = i * blockSize + offsetX, posY = j * blockSize + offsetY;
                g2D.fillRect(posX + 2, posY + 2, blockSize - 5, blockSize - 5);

                if (blockSize >= 7) {
                    g2D.setColor(Color.black);
                    g2D.drawRect(posX + 2, posY + 2, blockSize - 5, blockSize - 5);
                }
            }
        }
    }
	
    public void setGridOffset(Point offset) { this.gridOffset = offset; }
    public void updateGridDetails(int gridHeight, int gridSize, int blockSize) 
    { 
    	this.gridHeight = gridHeight;
    	this.gridSize = gridSize; 
    	this.blockSize = blockSize; 
    }
    
    public void setMouseDragPoint(Point mouseDragPoint) { this.mouseDragPoint = mouseDragPoint; }
    
    public Point getGridOffset() { return this.gridOffset; }
    public Point getMouseDragPoint() { return this.mouseDragPoint; }
}
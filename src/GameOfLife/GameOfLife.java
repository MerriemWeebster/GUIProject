package GameOfLife;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

public class GameOfLife
{

	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run()
			{
				JFrame frame = new JFrame("Game of Life");
				frame.setSize(1000, 720);
				frame.setResizable(true);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setVisible(true);
				frame.getContentPane().setBackground(Color.GRAY);
				
				GamePanel panel = new GamePanel();
				frame.add(panel);
			}
			
		});
	}
}

@SuppressWarnings("serial")
class GamePanel extends JPanel implements ActionListener
{
	public static final int GRID_SIZE = 5250, GRID_HEIGHT = 50, GRID_WIDTH = GRID_SIZE / GRID_HEIGHT;
		
	private int currentGeneration = 0, currentSpeed = 250, gridHeight, gridSize, blockSize;
	private int currentSize = 1;
	private String patterns[] = {"Clear", "Block", "Tub", "Boat", "Snake", "Ship", "Aircraft Carrier", "Beehive", "Barge", 
			"Python", "Long Boat", "Eater, Fishhook", "Loaf"},
			speeds[] = {"Slow", "Normal", "Fast"}, sizes[] = {"Small", "Medium", "Big"};
	private BorderLayout borderLayout;
	private FlowLayout flowLayout;
	private JPanel grid, controls;
	private Timer panelTimer, generationTimer;
	private JButton nextButton, startButton;
	private JLabel generationLabel;
	private JComboBox<String> patternList, speedList, sizeList;
	private JCheckBox editCheck;
	private LifeCell cells[];
	private Point mouseDragPoint, gridOffset = new Point(0, 0);

	GamePanel()
	{
		cells = new LifeCell[GRID_SIZE];
		
		for(int i = 0; i < GRID_SIZE; i++) cells[i] = new LifeCell(i, cells);			
		
		
		panelTimer = new Timer(10, this);
		panelTimer.start();
		
		borderLayout = new BorderLayout();
		flowLayout = new FlowLayout();
		nextButton = new JButton("Next");
		startButton = new JButton("Start");
		generationLabel = new JLabel("Generation: 0");
		
		generationTimer = new Timer(currentSpeed, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				currentGeneration++;
				updateGame();
			}
			
		});
		
		nextButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				currentGeneration++;
				updateGame();
			}
			
		});
		
		startButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(!generationTimer.isRunning())
				{
					generationTimer.start();
					nextButton.setEnabled(false);
					startButton.setText("Stop");
				}
				else
				{
					generationTimer.stop();
					nextButton.setEnabled(true);
					startButton.setText("Start");
				}
				
			}
			
		});
		
		
		patternList = new JComboBox<String>(patterns);
		patternList.setSelectedIndex(0);
		patternList.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				generationTimer.stop();
				nextButton.setEnabled(true);
				startButton.setText("Start");
				currentGeneration = 0;
				
				for(LifeCell cell : cells) cell.setAlive(false);
				
				int midX = gridSize / 2, midY = gridHeight / 2;
				int startingGrid = midX + midY * gridSize;
				
				switch((String) patternList.getSelectedItem())
				{
					case "Block":
						cells[startingGrid].setAlive(true);
						cells[startingGrid + 1].setAlive(true);
						cells[startingGrid + gridSize].setAlive(true);
						cells[startingGrid + gridSize + 1].setAlive(true);
						break;
					case "Tub":
						cells[startingGrid + 1].setAlive(true);
						cells[startingGrid - 1].setAlive(true);
						cells[startingGrid - gridSize].setAlive(true);
						cells[startingGrid + gridSize].setAlive(true);
						break;
					case "Boat":
						cells[startingGrid + 1].setAlive(true);
						cells[startingGrid - 1].setAlive(true);
						cells[startingGrid - gridSize].setAlive(true);
						cells[startingGrid + gridSize].setAlive(true);						
						cells[startingGrid + gridSize + 1].setAlive(true);
						break;
					case "Snake":
						cells[startingGrid - 1].setAlive(true);
						cells[startingGrid + 1].setAlive(true);
						cells[startingGrid + 2].setAlive(true);
						cells[startingGrid + gridSize + 2].setAlive(true);
						cells[startingGrid + gridSize ].setAlive(true);
						cells[startingGrid + gridSize - 1].setAlive(true);
						break;
					case "Ship":
						cells[startingGrid - 1].setAlive(true);
						cells[startingGrid + 1].setAlive(true);
						cells[startingGrid + gridSize].setAlive(true);
						cells[startingGrid + gridSize + 1].setAlive(true);
						cells[startingGrid - gridSize].setAlive(true);
						cells[startingGrid - gridSize - 1].setAlive(true);
						break;
					case "Aircraft Carrier":
						cells[startingGrid - 1].setAlive(true);
						cells[startingGrid + 2].setAlive(true);
						cells[startingGrid + gridSize + 2].setAlive(true);
						cells[startingGrid + gridSize + 1].setAlive(true);
						cells[startingGrid - gridSize].setAlive(true);
						cells[startingGrid - gridSize - 1].setAlive(true);
						break;
					case "Beehive":
						cells[startingGrid - 1].setAlive(true);
						cells[startingGrid + 2].setAlive(true);
						cells[startingGrid + gridSize].setAlive(true);
						cells[startingGrid + gridSize + 1].setAlive(true);
						cells[startingGrid - gridSize].setAlive(true);
						cells[startingGrid - gridSize + 1].setAlive(true);
						break;
					case "Barge":
						cells[startingGrid].setAlive(true);
						cells[startingGrid + 2].setAlive(true);
						cells[startingGrid + gridSize + 1].setAlive(true);
						cells[startingGrid - gridSize - 1].setAlive(true);
						cells[startingGrid - gridSize + 1].setAlive(true);
						cells[startingGrid - gridSize * 2].setAlive(true);
						break;
					case "Python":
						cells[startingGrid].setAlive(true);
						cells[startingGrid - 2].setAlive(true);
						cells[startingGrid + 2].setAlive(true);
						cells[startingGrid + gridSize - 1].setAlive(true);
						cells[startingGrid + gridSize - 2].setAlive(true);
						cells[startingGrid - gridSize + 1].setAlive(true);
						cells[startingGrid - gridSize + 2].setAlive(true);
						break;
					case "Long Boat":
						cells[startingGrid].setAlive(true);
						cells[startingGrid + 2].setAlive(true);
						cells[startingGrid + gridSize + 1].setAlive(true);
						cells[startingGrid + gridSize + 2].setAlive(true);
						cells[startingGrid - gridSize - 1].setAlive(true);
						cells[startingGrid - gridSize + 1].setAlive(true);
						cells[startingGrid - gridSize * 2].setAlive(true);
						break;
					case "Eater, Fishhook":
						cells[startingGrid + 1].setAlive(true);
						cells[startingGrid + gridSize + 1].setAlive(true);
						cells[startingGrid + gridSize + 2].setAlive(true);
						cells[startingGrid - gridSize - 1].setAlive(true);
						cells[startingGrid - gridSize + 1].setAlive(true);
						cells[startingGrid - gridSize * 2].setAlive(true);
						cells[startingGrid - gridSize * 2 - 1].setAlive(true);
						break;
					case "Loaf":
						cells[startingGrid - 1].setAlive(true);
						cells[startingGrid + 2].setAlive(true);
						cells[startingGrid + gridSize].setAlive(true);
						cells[startingGrid + gridSize + 1].setAlive(true);
						cells[startingGrid - gridSize - 1].setAlive(true);
						cells[startingGrid - gridSize + 1].setAlive(true);
						cells[startingGrid - gridSize * 2].setAlive(true);
						break;
				}
			}
			
		});
		
		speedList = new JComboBox<String>(speeds);
		speedList.setSelectedIndex(1);
		speedList.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				switch(speedList.getSelectedIndex())
				{
					case 0: 
						currentSpeed = 500;
						break;
					case 1:
						currentSpeed = 250;
						break;
					case 2:
						currentSpeed = 100;
						break;
				}
				
				generationTimer.setDelay(currentSpeed);
				if(generationTimer.isRunning()) generationTimer.restart();
			}
			
		});
		
		sizeList = new JComboBox<String>(sizes);
		sizeList.setSelectedIndex(1);
		sizeList.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				currentSize = sizeList.getSelectedIndex();
				if(currentSize == 0) gridOffset = new Point();
				updateSize();
				if(gridOffset.x >= (gridSize * blockSize) / 1.5) gridOffset.setLocation((gridSize * blockSize) / 2, gridOffset.y);
				if(gridOffset.x <= -(gridSize * blockSize) / 1.5) gridOffset.setLocation(-(gridSize * blockSize) / 2, gridOffset.y);

				if(gridOffset.y >= (gridHeight * blockSize) / 1.5) gridOffset.setLocation(gridOffset.x, (gridHeight * blockSize) / 2);
				if(gridOffset.y <= -(gridHeight * blockSize) / 1.5) gridOffset.setLocation(gridOffset.x, -(gridHeight * blockSize) / 2);
				repaint();
			}
			
		});
		
		editCheck = new JCheckBox("Edit Mode");
		
		this.setLayout(borderLayout);
		grid = new JPanel() {
			@Override
			public void paintComponent(Graphics g)
			{
				super.paintComponent(g);
				
				Graphics2D g2D = (Graphics2D) g;	
				//System.out.println("Width: " + gridSize + " Height: " + gridHeight + " Cells: " + totalCells);
				
				int offsetX = (this.getWidth() / 2) - ((gridSize * blockSize) / 2) + gridOffset.x, 
						offsetY = (this.getHeight() / 2) - ((gridHeight * blockSize) / 2) + gridOffset.y;

				for(int i = 0; i < gridSize; i++)
				{
					for(int j = 0; j < gridHeight; j++)
					{
						int index = i  + j * GRID_WIDTH;						
						
						g2D.setColor(Color.DARK_GRAY);

						if(cells[index].isAlive())
						{
							g2D.setColor(Color.YELLOW);
						}
						
						int posX = i * blockSize + offsetX, posY = j * blockSize + offsetY;
						
						g2D.fillRect(posX + 2, posY + 2, blockSize - 5, blockSize - 5);
						g2D.setColor(Color.black);
						g2D.drawRect(posX + 2, posY + 2, blockSize - 5, blockSize - 5);
					}
				}
			}
		};
		
		grid.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e)
			{
				if(editCheck.isSelected() && !generationTimer.isRunning())
				{
					int offsetX = (grid.getWidth() / 2) - ((gridSize * blockSize) / 2) + gridOffset.x, 
							offsetY = (grid.getHeight() / 2) - ((gridHeight * blockSize) / 2) + gridOffset.y;
					
					int selectedIndex = -1;
					
					for(int i = 0; i < gridSize; i++)
					{
						for(int j = 0; j < gridHeight; j++)
						{
							int index = i  + j * GRID_WIDTH;						
							int posX = i * blockSize + offsetX, posY = j * blockSize + offsetY;
							
							if(e.getPoint().getX() >= posX && e.getPoint().getX() <= posX + blockSize + 10
									&& e.getPoint().getY() >= posY && e.getPoint().getY() <= posY + blockSize + 10)
							{
								selectedIndex = index;
								break;
							}
						}
						
						if(selectedIndex > -1) break;
					}
					
					if(selectedIndex > -1)
					{
						cells[selectedIndex].setAlive(!cells[selectedIndex].isAlive());
					}
				}
			}

			@Override
			public void mousePressed(MouseEvent e)
			{
				setCursor(new Cursor(Cursor.HAND_CURSOR));
				mouseDragPoint = e.getPoint();
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				// TODO Auto-generated method stub
				
			}
			
		});
		
		grid.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseDragged(MouseEvent e)
			{					
				Point currentPoint = e.getPoint(),
				currentOffset = new Point(currentPoint.x - mouseDragPoint.x, currentPoint.y - mouseDragPoint.y);
				
				if(gridOffset.x >= (gridSize * blockSize) / 2 && currentOffset.x > 0) currentOffset.setLocation(0, currentOffset.y);
				if(gridOffset.x <= -(gridSize * blockSize) / 2 && currentOffset.x < 0) currentOffset.setLocation(0, currentOffset.y);

				if(gridOffset.y >= (gridHeight * blockSize) / 2 && currentOffset.y > 0) currentOffset.setLocation(currentOffset.x, 0);
				if(gridOffset.y <= -(gridHeight * blockSize) / 2 && currentOffset.y < 0) currentOffset.setLocation(currentOffset.x, 0);
				
				gridOffset = new Point(gridOffset.x + currentOffset.x, gridOffset.y + currentOffset.y);
				mouseDragPoint = e.getPoint();
			}

			@Override
			public void mouseMoved(MouseEvent e)
			{
				// TODO Auto-generated method stub
				
			}
			
		});
						
		controls = new JPanel();
		controls.setLayout(flowLayout);
		controls.add(editCheck);
		controls.add(patternList);
		controls.add(nextButton);
		controls.add(startButton);
		controls.add(speedList);
		controls.add(sizeList);
		controls.add(generationLabel);
		
		grid.setBackground(Color.GRAY);
		controls.setBackground(Color.GRAY);
		
		this.add(grid, BorderLayout.CENTER);
		this.add(controls, BorderLayout.PAGE_END);
	}
	
	public int getGeneration() { return this.currentGeneration; }
	public int getSpeed() { return this.currentSpeed; }

	public void updateGame()
	{
		for(LifeCell cell : cells) cell.updateCell();
		for(LifeCell cell : cells) cell.updateAliveState();
		repaint();
	}
	
	public void updateSize()
	{
		gridHeight = GRID_HEIGHT;
		gridSize = GRID_WIDTH;
		blockSize = grid.getHeight() / gridHeight;
		
		if(blockSize * gridSize > grid.getWidth()) blockSize = grid.getWidth() / gridSize;

		if(currentSize == 1) { blockSize *= 2; }
		if(currentSize == 2) { blockSize *= 4;}
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		updateSize();
		generationLabel.setText("Generation: " + currentGeneration);
		repaint();
	}
}

class LifeCell
{
	private int xPos, yPos;
	private boolean alive = false, futureAlive = false;
	private LifeCell cells[];
	
	
	LifeCell(int currentIndex, LifeCell cells[])
	{
		this.xPos = currentIndex % GamePanel.GRID_WIDTH;
		this.yPos = currentIndex / GamePanel.GRID_WIDTH;
		this.cells = cells;
	}
	
	public int getX() { return xPos; }
	public int getY() { return yPos; }
	
	public int getIndex(int x, int y)
	{
		int index = x + y * GamePanel.GRID_WIDTH;
		return index;
	}
	
	public boolean isAlive() { return alive; }
	
	public void setAlive(boolean alive) { this.alive = this.futureAlive = alive; }
	public void updateAliveState() { this.alive = this.futureAlive; }

	public void updateCell()
	{
		int neighbours = 0;
		
		if(xPos < GamePanel.GRID_WIDTH - 1)
		{
			if(cells[getIndex(xPos + 1, yPos)].isAlive()) neighbours++;
		}
		
		if(xPos > 0)
		{
			if(cells[getIndex(xPos - 1, yPos)].isAlive()) neighbours++;
		}
		
		if(yPos < GamePanel.GRID_HEIGHT - 1)
		{
			if(cells[getIndex(xPos, yPos + 1)].isAlive()) neighbours++;
		}
		
		if(yPos > 0)
		{
			if(cells[getIndex(xPos, yPos - 1)].isAlive()) neighbours++;
		}
		
		if(neighbours < 2 || neighbours > 3) futureAlive = false;
		else futureAlive = true;		
	}
}
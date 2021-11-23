package GameOfLife;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
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

class GamePanel extends JPanel implements ActionListener
{
	public static final int GRID_SIZE = 525, GRID_HEIGHT = 21, GRID_WIDTH = GRID_SIZE / GRID_HEIGHT;
	
	private int currentGeneration = 0, currentSpeed = 2000;
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
	private LifeCell cells[];

	@SuppressWarnings("serial")
	GamePanel()
	{
		cells = new LifeCell[GRID_SIZE];
		
		for(int i = 0; i < GRID_SIZE; i++)
		{
			cells[i] = new LifeCell(i, cells, this);
		}
		
		panelTimer = new Timer(10, this);
		panelTimer.start();
		
		borderLayout = new BorderLayout();
		flowLayout = new FlowLayout();
		nextButton = new JButton("Next");
		startButton = new JButton("Start");
		generationLabel = new JLabel("Generation: 0");
		
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
				if(startButton.getText().equals("Start"))
				{
					generationTimer = new Timer(currentSpeed, new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e)
						{
							currentGeneration++;
							updateGame();
						}
						
					});
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
				// TODO Auto-generated method stub
				
			}
			
		});
		
		speedList = new JComboBox<String>(speeds);
		speedList.setSelectedIndex(1);
		speedList.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				// TODO Auto-generated method stub
				
			}
			
		});
		
		sizeList = new JComboBox<String>(sizes);
		sizeList.setSelectedIndex(1);
		sizeList.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				currentSize = sizeList.getSelectedIndex();
				repaint();
			}
			
		});
		
		this.setLayout(borderLayout);
		grid = new JPanel() {
			@Override
			public void paintComponent(Graphics g)
			{
				super.paintComponent(g);
				
				Graphics2D g2D = (Graphics2D) g;
				int gridHeight = GRID_HEIGHT, gridSize = GRID_WIDTH;
				if(currentSize == 1) { gridSize /= 1.9; gridHeight /= 1.9; }
				if(currentSize == 2) { gridSize /= 2.75; gridHeight /= 2.75; }
				
				int totalCells = gridSize * gridHeight;
				int blockSize = this.getHeight() / gridHeight;
				if(blockSize * gridSize > this.getWidth()) blockSize = this.getWidth() / gridSize;
				
				//System.out.println("Width: " + gridSize + " Height: " + gridHeight + " Cells: " + totalCells);
				
				int offsetX = (this.getWidth() / 2) - ((gridSize * blockSize) / 2), 
						offsetY = (this.getHeight() / 2) - ((gridHeight * blockSize) / 2);

				for(int i = 0; i < gridSize; i++)
				{
					for(int j = 0; j < gridHeight; j++)
					{
						int heightOffset = (GRID_HEIGHT - gridHeight) / 2, 
								widthOffset = (GRID_WIDTH - gridSize) / 2;
						int index = ((i + widthOffset) + (j + heightOffset) * GRID_WIDTH);						
						
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
						
		controls = new JPanel();
		controls.setLayout(flowLayout);
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
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		generationLabel.setText("Generation: " + currentGeneration);
		repaint();
	}
}

class LifeCell
{
	private int xPos, yPos, generation;
	private boolean alive = false, futureAlive = false;
	private LifeCell cells[];
	private GamePanel gamePanel;
	
	
	LifeCell(int currentIndex, LifeCell cells[], GamePanel gamePanel)
	{
		this.xPos = currentIndex % GamePanel.GRID_WIDTH;
		this.yPos = currentIndex / GamePanel.GRID_WIDTH;
		this.cells = cells;
		this.gamePanel = gamePanel;
		this.generation = 0;
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
		
		generation++;
	}
}
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class CooperationServerThreadObjectOutput implements Runnable {

	private CooperationServer server;
	private ObjectOutputStream outObject;
	private Socket client;

	public CooperationServerThreadObjectOutput(CooperationServer cooperationServer, Socket client) throws IOException {
		this.server = cooperationServer;
		this.client = client;
		this.outObject = new ObjectOutputStream(client.getOutputStream());
	}

	public void run() {
	}

	public void sendMap() {
		try {
			System.out.println("Sending map1");
			Tile[][] temp = server.getTileMap();
			System.out.println("Sending map2");
			outObject.writeObject(cloneTileMap(temp));
			System.out.println("Sending map3");
			outObject.reset();
			System.gc();
			System.out.println("Sending map finished");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Tile[][] cloneTileMap(Tile[][] tileMap) {
		Tile[][] temp = new Tile[tileMap.length][tileMap[0].length];
		for (int i = 0; i < temp.length; i++) {
			for (int j = 0; j < temp[i].length; j++) {
				// tileMap[i][j].draw(i, j, gc);
				if (tileMap[i][j] instanceof Grass) {
					temp[i][j] = new Grass();
				} else if (tileMap[i][j] instanceof GrassB) {
					temp[i][j] = new GrassB();
				} else if (tileMap[i][j] instanceof Path) {
					temp[i][j] = new Path();
				} else if (tileMap[i][j] instanceof PathB) {
					temp[i][j] = new PathB();
				} else if (tileMap[i][j] instanceof Tunnel) {
					temp[i][j] = new Tunnel();
				} else if (tileMap[i][j] instanceof TunnelB) {
					temp[i][j] = new TunnelB();
				}

				for (Rat r : tileMap[i][j].getOccupantRats()) {
					if (r instanceof AdultMale) {
						temp[i][j].addOccupantRat(new AdultMale(null, r.getSpeed(), r.getDirection(), r.getGasTimer(),
								r.getXPos(), r.getYPos(), ((AdultMale) r).getFertile()));
					} else if (r instanceof AdultFemale) {
						temp[i][j].addOccupantRat(new AdultFemale(null, r.getSpeed(), r.getDirection(), r.getGasTimer(),
								r.getXPos(), r.getYPos(), ((AdultFemale) r).getFertile(), ((AdultFemale) r).getPregnancyTime(), ((AdultFemale) r).getRatFetusCount()));
					} else if (r instanceof AdultIntersex) {
						temp[i][j].addOccupantRat(new AdultIntersex(null, r.getSpeed(), r.getDirection(), r.getGasTimer(),
								r.getXPos(), r.getYPos(), ((AdultIntersex) r).getFertile(), ((AdultIntersex) r).getPregnancyTime(), ((AdultIntersex) r).getRatFetusCount()));
					} else if (r instanceof DeathRat) {
						temp[i][j].addOccupantRat(new DeathRat(null, r.getSpeed(), r.getDirection(), r.getGasTimer(),
								r.getXPos(), r.getYPos(), ((DeathRat) r).getKillCounter()));
					} else if (r instanceof ChildRat) {
						temp[i][j].addOccupantRat(new ChildRat(null, r.getSpeed(), r.getDirection(), r.getGasTimer(),
								r.getXPos(), r.getYPos(), ((ChildRat) r).getFertile(), ((ChildRat) r).getAge(), ((ChildRat) r).getRatSex()));						
					}
					
				}
			}
		}
		return temp;
	}

	private Tile[][] removeControllerFromMap(Tile[][] tileMap) {
		Tile[][] tempTileMap = tileMap.clone();
		System.out.println(tileMap + " " + tempTileMap + " " + (tileMap == tempTileMap));
		for (Tile[] tileList : tempTileMap) {
			for (Tile t : tileList) {
				t.setController(null);

				for (Power p : t.getActivePowers()) {
					p.setController(null);
				}

				for (Rat r : t.getOccupantRats()) {
					r.setController(null);
				}
			}
		}
		return tempTileMap;
	}
}

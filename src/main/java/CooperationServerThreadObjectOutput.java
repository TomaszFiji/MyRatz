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
			Tile[][] temp = server.getTileMap();
			outObject.writeObject(cloneTileMap(temp));
			outObject.reset();
			System.gc();
//			System.out.println("Sending map finished");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendItems() {
		try {
			int[] temp = server.getCounters();
			outObject.writeObject(temp);
			outObject.reset();
			System.gc();
//			System.out.println("Sending items finished");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendTimeAndRatCounters(TimeAndRatCounters temp) {
		try {
			outObject.writeObject(temp);
			outObject.reset();
			System.gc();
//			System.out.println("Sending time and counters finished");
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
								r.getXPos(), r.getYPos(), ((AdultFemale) r).getFertile(),
								((AdultFemale) r).getPregnancyTime(), ((AdultFemale) r).getRatFetusCount()));
					} else if (r instanceof AdultIntersex) {
						temp[i][j].addOccupantRat(new AdultIntersex(null, r.getSpeed(), r.getDirection(),
								r.getGasTimer(), r.getXPos(), r.getYPos(), ((AdultIntersex) r).getFertile(),
								((AdultIntersex) r).getPregnancyTime(), ((AdultIntersex) r).getRatFetusCount()));
					} else if (r instanceof DeathRat) {
						temp[i][j].addOccupantRat(new DeathRat(null, r.getSpeed(), r.getDirection(), r.getGasTimer(),
								r.getXPos(), r.getYPos(), ((DeathRat) r).getKillCounter()));
					} else if (r instanceof ChildRat) {
						temp[i][j].addOccupantRat(new ChildRat(null, r.getSpeed(), r.getDirection(), r.getGasTimer(),
								r.getXPos(), r.getYPos(), ((ChildRat) r).getFertile(), ((ChildRat) r).getAge(),
								((ChildRat) r).getRatSex()));
					}

				}

				for (Power p : tileMap[i][j].getActivePowers()) {
					if (p instanceof Bomb) {
						temp[i][j]
								.addActivePower(new Bomb(null, p.getXPos(), p.getYPos(), ((Bomb) p).getTicksActive()));
					}
					if (p instanceof FemaleSwapper) {
						temp[i][j].addActivePower(new FemaleSwapper(null, p.getXPos(), p.getYPos()));
					}
					if (p instanceof Gas) {
						temp[i][j].addActivePower(new Gas(null, p.getXPos(), p.getYPos(), ((Gas) p).isOriginal()));
					}
					if (p instanceof MaleSwapper) {
						temp[i][j].addActivePower(new MaleSwapper(null, p.getXPos(), p.getYPos()));
					}
					if (p instanceof Poison) {
						temp[i][j].addActivePower(new Poison(null, p.getXPos(), p.getYPos()));
					}
					if (p instanceof Sterilisation) {
						temp[i][j].addActivePower(new Sterilisation(null, p.getXPos(), p.getYPos()));
					}
					if (p instanceof StopSign) {
						temp[i][j].addActivePower(new StopSign(null, p.getXPos(), p.getYPos(), ((StopSign) p).getHP()));
					}

				}
			}
		}
		return temp;
	}

	private Tile[][] removeControllerFromMap(Tile[][] tileMap) {
		Tile[][] tempTileMap = tileMap.clone();
//		System.out.println(tileMap + " " + tempTileMap + " " + (tileMap == tempTileMap));
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

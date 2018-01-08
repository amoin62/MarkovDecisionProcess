package matrixFactorization.coordinate;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;

import mdp.Main;



public class CartesianCoordinate implements ICoordinate<CartesianCoordinate>,
		Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static public final double maxMoveDistance = 5.;
	static public final double minMoveDistance = 0.001;
	static public int nbDimension = Main.DIMENSION;
	static public final double defaultMoveDist = 0.3;
	static public final double faster = 0.5;
	static public final double slower = 0.5;
	protected double moveDistance;
	protected float[] position;
	protected double[] forceVect;
	protected double[] previousForceVect;
	
	public CartesianCoordinate() {
		this.position = new float[nbDimension];
		this.forceVect = null;
		this.previousForceVect = null;
		// Arrays.fill(this.position, 0);
		this.rand();
		this.moveDistance = defaultMoveDist;
	}
	
	public CartesianCoordinate(long seed) {
		this.position = new float[nbDimension];
		this.forceVect = null;
		this.previousForceVect = null;
		// Arrays.fill(this.position, 0);
		this.rand(seed);
		this.moveDistance = defaultMoveDist;
	}
	
	public CartesianCoordinate(CartesianCoordinate c) {
		this.position = Arrays.copyOf(c.position, c.position.length);
		this.forceVect = null;
		this.previousForceVect = null;
		this.moveDistance = defaultMoveDist;
	}

	public CartesianCoordinate(float[] coord) {
		this();
		for (int i = 0; i < nbDimension && i < coord.length; i++) {
			this.position[i] = coord[i];
		}
	}

	public void plus(CartesianCoordinate c) {
		for (int i = 0; i < nbDimension; i++) {
			position[i] += c.position[i];
		}
	}

	public void minus(CartesianCoordinate c) {
		for (int i = 0; i < nbDimension; i++) {
			position[i] -= c.position[i];
		}
	}

	public void times(double d) {
		for (int i = 0; i < nbDimension; i++) {
			position[i] *= d;
		}
	}

	public void div(double d) {
		for (int i = 0; i < nbDimension; i++) {
			position[i] /= d;
		}
	}

	public void applyForce() {
		if (this.forceVect != null) {
			double squareDist = 0;
			for (int i = 0; i < nbDimension; i++) {
				squareDist += this.forceVect[i] * this.forceVect[i];
			}
			if (squareDist != 0) {
				double dist = Math.sqrt(squareDist);
				for (int i = 0; i < nbDimension; i++) {
					this.forceVect[i] /= dist;
				}
				if (this.previousForceVect != null) {
					double scalar = 0;
					for (int i = 0; i < nbDimension; i++) {
						scalar += this.forceVect[i] * this.previousForceVect[i];
					}
					if (scalar > 0) {
						this.moveDistance += this.moveDistance * scalar
								* faster;
					} else if (scalar < 0) {
						this.moveDistance += this.moveDistance * scalar
								* slower;
					}
					this.moveDistance = Math.max(minMoveDistance, Math.min(
							maxMoveDistance, this.moveDistance));
				}
				for (int i = 0; i < nbDimension; i++) {
					this.position[i] += this.moveDistance * this.forceVect[i];
				}
			}
			double[] temp = this.previousForceVect;
			this.previousForceVect = this.forceVect;
			this.forceVect = temp;
			if (this.forceVect != null) {
				Arrays.fill(this.forceVect, 0);
			}
		}
	}

	public double distance(CartesianCoordinate c) {
		double squareDist = 0;
		for (int i = 0; i < nbDimension; i++) {
			double diff = this.position[i] - c.position[i];
			squareDist += diff * diff;
		}
		return Math.sqrt(squareDist);
	}

	public void normalize() {
		double squareDist = 0;
		for (int i = 0; i < nbDimension; i++) {
			squareDist += this.position[i] * this.position[i];
		}
		double dist = Math.sqrt(squareDist);
		if (dist != 0) {
			this.div(dist);
		}
	}

	public double scalar(CartesianCoordinate c) {
		double scalar = 0;
		for (int i = 0; i < nbDimension; i++) {
			scalar += this.position[i] * c.position[i];
		}
		return scalar;
	}

	public void storeForce(CartesianCoordinate c, double value) {
		if (this.forceVect == null) {
			this.forceVect = new double[nbDimension];
		}
		CartesianCoordinate movementVector = (CartesianCoordinate) c.clone();
		movementVector.minus(this);
		while (movementVector.isOrigin()) {
			if (value >= 0) {
				return;
			} else {
				movementVector.rand();
			}
		}
		movementVector.normalize();
		for (int i = 0; i < nbDimension; i++) {
			this.forceVect[i] += (movementVector.position[i] * value);
		}
	}

	public Point2D to2D() {
		if (nbDimension < 2) {
			return new Point2D.Double(this.position[0], 0);
		} else {
			return new Point2D.Double(this.position[0], this.position[1]);
		}
	}

	@Override
	protected Object clone() {
		CartesianCoordinate clone = null;
		try {
			clone = (CartesianCoordinate) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		clone.forceVect = null;
		clone.previousForceVect = null;
		clone.position = Arrays.copyOf(this.position, this.position.length);
		clone.moveDistance = defaultMoveDist;
		return clone;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < nbDimension; i++) {
			sb.append(this.position[i] + " ");
		}
		// StringBuffer sb = new StringBuffer("(");
		// for (int i = 0; i < nbDimension - 1; i++) {
		// sb.append(this.position[i]);
		// sb.append(",");
		// }
		// sb.append(this.position[nbDimension - 1]);
		// sb.append(")");
		return sb.toString();
	}

	public String toStringInteger(double add, double mul) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < nbDimension; i++) {
			sb.append(((int) ((this.position[i] + add) * mul)) + " ");
		}
		// StringBuffer sb = new StringBuffer("(");
		// for (int i = 0; i < nbDimension - 1; i++) {
		// sb.append(this.position[i]);
		// sb.append(",");
		// }
		// sb.append(this.position[nbDimension - 1]);
		// sb.append(")");
		return sb.toString();
	}

	private boolean isOrigin() {
		for (int i = 0; i < nbDimension; i++) {
			if (this.position[i] != 0) {
				return false;
			}
		}
		return true;
	}

	private void rand() {
		for (int i = 0; i < nbDimension; i++) {
			this.position[i] = (float) (Math.random() - 0.5);
		}
	}
	
	private void rand(long seed) {
		Random rand = new Random(seed);
		for (int i = 0; i < nbDimension; i++) {
			this.position[i] = (float) (rand.nextDouble() - 0.5);
		}
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(position);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		CartesianCoordinate other = (CartesianCoordinate) obj;
		if (!Arrays.equals(position, other.position)) {
			return false;
		}
		return true;
	}

	public void initialize(Point2D p) {
		this.position[0] = (float) p.getX();
		if (nbDimension > 1) {
			this.position[1] = (float) p.getY();
		}
	}

	@Override
	public double getLastMove() {
		return this.moveDistance;
	}

	@Override
	public void moveInRandomDir(double dist) {
		CartesianCoordinate dir = new CartesianCoordinate();
		dir.normalize();
		for (int i = 0; i < nbDimension; i++) {
			this.position[i] += dist * dir.position[i];
		}
	}

	@Override
	public void setPosition(CartesianCoordinate c) {
		for (int i = 0; i < nbDimension; i++) {
			this.position[i] = c.position[i];
		}

	}
	
	public void setPosition(float[] newPos){
		for (int i = 0; i < nbDimension; i++) {
			this.position[i] = (float) newPos[i];
		}
	}
	
	public float[] getPosition(){
		return this.position;
		
	}

	@Override
	public void toGravCenter(Iterable<CartesianCoordinate> pos) {
		int nbDiv = 0;
		for (int i = 0; i < nbDimension; i++) {
			this.position[i] = 0;
		}
		for (CartesianCoordinate c : pos) {
			for (int i = 0; i < nbDimension; i++) {
				this.position[i] += c.position[i];
			}
			nbDiv++;
		}
		if (nbDiv != 0) {
			for (int i = 0; i < nbDimension; i++) {
				this.position[i] = this.position[i] / nbDiv;
			}
		}
	}

	@Override
	public void setMoveDist(double d) {
		this.moveDistance = d;
	}

	public void addOnDim(int d, double v) {
		this.position[d] += v;
	}

	public void makePerpendicular() {
		if (nbDimension % 2 == 0) {
			for (int i = 0; i < nbDimension; i += 2) {
				float temp = this.position[i];
				this.position[i] = this.position[i + 1];
				this.position[i + 1] = -temp;
			}
		} else {
			int nonNull = -1;
			for (int i = 0; i < nbDimension; i++) {
				if (this.position[i] != 0) {
					nonNull = i;
					break;
				}
			}
			if (nonNull == -1) {
				return;
			} else {
				int i = 0;
				boolean doneAfter = false;
				System.out.println(nonNull);
				while (i < nbDimension) {
					if (i == nonNull) {
						this.position[i] = 0;
						i++;
					} else if (i == nonNull - 1) {
						float temp = this.position[i];
						this.position[i] = this.position[i + 2];
						this.position[i + 2] = -temp;
						i++;
						doneAfter = true;
					} else if (i == (nonNull + 1) && doneAfter) {
						i++;
					} else {
						float temp = this.position[i];
						this.position[i] = this.position[i + 1];
						this.position[i + 1] = -temp;
						i += 2;
					}
				}
			}
		}
	}
	
	public float innerProduct(float[] c){
		float res =0;
		for (int i=0;i<this.position.length;i++){
			res += (this.position[i]*c[i]);
		}
		return res;
	}
	
	public float[] vectorByScalar( double scalar){
		float[] res = Arrays.copyOf(this.position, this.position.length);
		for(int i=0;i<this.position.length;i++){
			res[i] *= scalar;
		}
		return res;
	}
	
	public double angle(CartesianCoordinate center){
		if(CartesianCoordinate.nbDimension != 2){
			System.err.println("Number of dimensions is not 2!");
			System.exit(1);
		}
		double slope = (this.position[1] - center.position[1]) / (this.position[0] - center.position[0]);
		double angle = Math.atan(slope);
		if((this.position[1] - center.position[1] > 0.0) && (this.position[0] - center.position[0] < 0.0)){
			angle = Math.PI - angle;
		}
		if((this.position[1] - center.position[1] < 0.0) && (this.position[0] - center.position[0] < 0.0)){
			angle = Math.PI + angle;
		}
		return angle;
	}
	
	public static void main(String[] args) {
		nbDimension = 9;
		CartesianCoordinate c = new CartesianCoordinate();
		c.position[0] = 0;
		System.out.println(c);
		CartesianCoordinate c2 = new CartesianCoordinate(c);
		c2.makePerpendicular();
		System.out.println(c2);
		System.out.println(c.scalar(c2));
	}
	
	

}

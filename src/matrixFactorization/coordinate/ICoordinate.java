package matrixFactorization.coordinate;

import java.awt.geom.Point2D;

public interface ICoordinate<C extends ICoordinate<C>> extends Cloneable {
	public void setMoveDist(double d);

	public double getLastMove();

	public double scalar(C c);

	public void normalize();

	public double distance(C c);

	public void storeForce(C c, double value);

	public void applyForce();

	public Point2D to2D();

	public void initialize(Point2D p);

	public void moveInRandomDir(double dist);

	public void setPosition(C c);

	public void toGravCenter(Iterable<C> pos);
}

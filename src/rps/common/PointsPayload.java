package rps.common;

public class PointsPayload extends Payload {
    private static final long serialVersionUID = 1L;
    private int points;

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
}

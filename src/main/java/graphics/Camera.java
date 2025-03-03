package graphics;

public class Camera {
    private float x, y, z;
    private float yaw = -90.0f; // Rotazione orizzontale (inizia guardando lungo l'asse Z negativo)
    private float pitch = 0.0f; // Rotazione verticale
    private float[] direction = new float[3]; // Vettore di direzione della camera

    public Camera(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        updateCameraDirection(); // Inizializza il vettore direzione
    }

    /**
     * Aggiorna il vettore direzione basandosi sugli angoli yaw e pitch
     */
    public void updateCameraDirection() {
        // Converte gli angoli di Eulero in vettore di direzione
        direction[0] = (float) (Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        direction[1] = (float) Math.sin(Math.toRadians(pitch));
        direction[2] = (float) (Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));

        // Normalizza il vettore
        float length = (float) Math
                .sqrt(direction[0] * direction[0] + direction[1] * direction[1] + direction[2] * direction[2]);
        direction[0] /= length;
        direction[1] /= length;
        direction[2] /= length;
    }

    /**
     * Muove la camera relativamente alla sua direzione
     */
    public void move(float dx, float dy, float dz) {
        // Movimento verticale (asse Y) rimane invariato
        y += dy;

        // Per il movimento avanti/indietro, usa la direzione della camera
        x += direction[0] * dz;
        z += direction[2] * dz;

        // Per il movimento laterale, calcola il vettore destro (perpendicolare alla
        // direzione)
        float[] right = new float[3];
        right[0] = (float) Math.cos(Math.toRadians(yaw - 90));
        right[1] = 0;
        right[2] = (float) Math.sin(Math.toRadians(yaw - 90));

        // Normalizza il vettore destro
        float length = (float) Math.sqrt(right[0] * right[0] + right[1] * right[1] + right[2] * right[2]);
        right[0] /= length;
        right[1] /= length;
        right[2] /= length;

        // Movimento laterale usando il vettore destro
        x += right[0] * dx;
        z += right[2] * dx;
    }

    /**
     * Ruota la camera in base al movimento del mouse
     */
    public void rotate(float deltaYaw, float deltaPitch) {
        yaw += deltaYaw;
        pitch += deltaPitch;

        // Limita il pitch per evitare il capovolgimento della camera
        if (pitch > 89.0f) {
            pitch = 89.0f;
        }
        if (pitch < -89.0f) {
            pitch = -89.0f;
        }

        // Aggiorna il vettore direzione
        updateCameraDirection();
    }

    // Getters e setters
    public float getX() {
        return this.x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return this.y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return this.z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
        updateCameraDirection();
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
        updateCameraDirection();
    }

    public float[] getDirection() {
        return direction;
    }
}
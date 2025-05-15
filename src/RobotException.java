class RobotException extends Exception {
    public RobotException(String message) {
        super(message);
    }
    
    public RobotException(String message, Throwable cause) {
        super(message, cause);
    }
}
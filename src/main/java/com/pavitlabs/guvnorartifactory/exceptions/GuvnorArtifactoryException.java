package gov.utah.dts.erep.guvnorartifactory.exceptions;

public class GuvnorArtifactoryException extends RuntimeException {

    private static final long serialVersionUID = 3520130196792939900L;

    public GuvnorArtifactoryException() {
        super();
    }

    public GuvnorArtifactoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public GuvnorArtifactoryException(String message) {
        super(message);
    }

    public GuvnorArtifactoryException(Throwable cause) {
        super(cause);
    }

}

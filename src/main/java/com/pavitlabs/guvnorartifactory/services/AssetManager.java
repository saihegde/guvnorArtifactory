package gov.utah.dts.erep.guvnorartifactory.services;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
/**
 * @author Sai Hegde
 * @since October 11, 2013
 * */
public interface AssetManager {

    /**
     * Lists all assets that the Guvnor hosts.
     * @return all Assets that the guvnor hosts.
     * */
    List<String> listAll();

    /**
     * Lists assets that the Guvnor hosts for the given package.
     * @param package name
     * @return Assets in the given package
     * */
    List<String> list(String packageName);

    /**
     * Returns <code>true</code> if asset exists in the given package.
     * @param package name 
     * @param asset name
     * */
    boolean exists(String packageName, String assetName);
    
    /**
     * Creates/Updates an asset in the package name provided.
     * @param package name 
     * @param asset that needs to be created
     * */
    void save(String packageName, File asset);
    
    /**
     * Deletes the asset in the package name provided.
     * @param package name 
     * @param asset that needs to be deleted
     * */
    void delete(String packageName, String assetName);
    
    /**
     * Utility method that returns true if a given file has to be considered as a guvnor asset/artifact.
     * @param asset file
    */
    boolean isGuvnorArtifact(File asset);
    
}

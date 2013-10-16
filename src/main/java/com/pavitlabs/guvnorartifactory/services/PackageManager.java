package gov.utah.dts.erep.guvnorartifactory.services;

import gov.utah.dts.erep.guvnorartifactory.exceptions.GuvnorArtifactoryException;

import java.io.File;
import java.util.List;

/**
 * @author Sai Hegde
 * @since October 11, 2013
 * */
public interface PackageManager {

    /**
     * Returns the list of packages that the Guvnor serves.
     * @return knowledge base package names in Guvnor.
     * */
    List<String> list() throws GuvnorArtifactoryException;

    /**
     * Returns <code>true</code> package exists in Guvnor.
     * @param packageName
     * @return boolean value to indicate if package exists in Guvnor
     * */
    boolean contains(String packageName) throws GuvnorArtifactoryException;

    /**
     * Creates a empty package in Guvnor.
     * @param packageName
     * */
    void create(String packageName) throws GuvnorArtifactoryException;

    /**
     * Deletes the package from Guvnor.
     * @param packageName
     * */
    void delete(String packageName) throws GuvnorArtifactoryException;
    
    /**
     * Uploads the model jar for that package.
     * @param packageName
     * @param Model jar for the package
     * */
    void uploadModel(String packageName, File modeljar) throws GuvnorArtifactoryException;
    
    /**
     * Updates Guvnor with assets from the supplied resource folder.
     * @param File location where the assets would be sourced from
     * @param package Names that need to be updated
     * */
    void update(File resourcesFolder, String... packageNames) throws GuvnorArtifactoryException;
    
    /**
     * Detect corrupted files/files preventing a snapshot build.
     * @param File location where the assets would be sourced from
     * @param comma separated package Names that need to be repaired
     * */
    void diagnoseForCorruptedAssets(File resourcesFolder, String... packageNames);
    
    /**
     * Builds packages. 
     * @param package Names that need to be built
     * @param snapshot name
     * */
    void build(String... packageNames) throws GuvnorArtifactoryException;
    
    /**
     * Builds a package snapshot.
     * @param package Names that need to be updated
     * @param snapshot name
     * */
    void buildSnapshot(String packageName, String snapshotName) throws GuvnorArtifactoryException;

}

package net.csibio.propro.utils;

import org.apache.commons.io.FilenameUtils;

public class RepositoryUtil {

    public static String repository;

    public static String getRepo(){
        return repository;
    }

    public static String getProjectRepo(String projectName){
        return FilenameUtils.concat(repository, projectName);
    }

    public static String getLibraryRepo(){
        return FilenameUtils.concat(FilenameUtils.concat(repository, "Library"), "Standard");
    }

    public static String getIrtLibraryRepo(){
        return FilenameUtils.concat(FilenameUtils.concat(repository, "Library"), "Irt");
    }

    public static String getProjectTempRepo(String projectName){
        return FilenameUtils.concat(getProjectRepo(projectName), "temp");
    }

    public static String buildOutputPath(String projectName, String fileName){
        String folderPath = FilenameUtils.concat(repository, projectName);
        return FilenameUtils.concat(folderPath, fileName);
    }
}

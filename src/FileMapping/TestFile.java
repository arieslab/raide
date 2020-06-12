package FileMapping;

import org.apache.commons.lang3.StringUtils;

import FileDetector.RAIDEUtils;

public class TestFile {
    private String filePath, productionFilePath;
    private String splitType;
    String[] data;

    public String getFileName() {
        return data[data.length - 1];
    }

    public String getFilePath() {
        return filePath;
    }

    public String getProductionFilePath() {
        return productionFilePath;
    }

    public void setProductionFilePath(String productionFilePath) {
        this.productionFilePath = productionFilePath;
    }

    public String getProjectRootFolder() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            stringBuilder.append(data[i] + RAIDEUtils.pathSeparator());
        }
        return stringBuilder.toString();
    }

    public String getAppName() {
        return data[3];
    }

    public String getTagName() {
        return data[4];
    }

    public TestFile(String filePath) {
        this.filePath = filePath;
        
        if (RAIDEUtils.isWindowsPath()) {
        	splitType = "\\\\";
        } else {
        	splitType = "/";
        }
        
        data = filePath.split(splitType);
    }

    public String getRelativeTestFilePath(){
        String[] splitString = filePath.split(splitType);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            stringBuilder.append(splitString[i] + RAIDEUtils.pathSeparator());
        }
        return filePath.substring(stringBuilder.toString().length()).replace("\\","/");
    }

    public String getRelativeProductionFilePath(){
        if (!StringUtils.isEmpty(productionFilePath)){
            String[] splitString = productionFilePath.split(splitType);
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < 5; i++) {
                stringBuilder.append(splitString[i] + RAIDEUtils.pathSeparator());
            }
            return productionFilePath.substring(stringBuilder.toString().length()).replace("\\","/");
        }
        else{
            return "";
        }

    }
}

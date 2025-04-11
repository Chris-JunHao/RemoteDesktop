package cn.edu.zjut.dockermanager.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

public class FileUtil {

    /**
     * 复制单个文件
     * 通过输入流和输出流，将源文件的内容复制到目标文件。
     * @param sourcePath 源文件路径
     * @param targetPath 目标文件路径
     * @throws IOException 如果复制过程中出现 I/O 异常，抛出此异常
     */
    public static void copyFile(String sourcePath, String targetPath) throws IOException {
        try (FileInputStream fis = new FileInputStream(sourcePath);
             FileOutputStream fos = new FileOutputStream(targetPath)) {
            byte[] bytes = new byte[1024];
            int len;
            while ((len = fis.read(bytes)) != -1) {
                fos.write(bytes, 0, len);
            }
        }
    }

    /**
     * 复制整个文件夹及其内容
     * 递归地复制文件夹及其中的所有文件和子文件夹。
     * @param sourcePath 源文件夹路径
     * @param targetPath 目标文件夹路径
     * @throws IOException 如果复制过程中出现 I/O 异常，抛出此异常
     */
    public static void copyDictionary(String sourcePath, String targetPath) throws IOException {
        File sourceFile = new File(sourcePath);
        if (!sourceFile.exists()) {
            throw new FileNotFoundException(); // 如果源文件夹不存在，抛出文件未找到异常
        }
        File targetFile = new File(targetPath);
        targetFile.mkdirs(); // 创建目标文件夹
        File[] fileList = sourceFile.listFiles(); // 获取源文件夹中的所有文件
        if (fileList != null && fileList.length > 0) {
            for (File f : fileList) {
                if (f.isDirectory()) {
                    // 递归复制子文件夹
                    copyDictionary(f.getAbsolutePath(), targetPath + File.separator + f.getName());
                } else {
                    // 复制文件
                    copyFile(f.getAbsolutePath(), targetPath + File.separator + f.getName());
                }
            }
        }
    }

    /**
     * 删除文件夹及其内容
     * 递归地删除文件夹及其中的所有文件和子文件夹。
     * @param filePath 文件夹路径
     * @throws IOException 如果删除过程中出现 I/O 异常，抛出此异常
     */
    public static void deleteDictionary(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        try (Stream<Path> walk = Files.walk(path)) {
            walk.sorted(Comparator.reverseOrder()) // 先删除文件夹中的内容，再删除文件夹本身
                    .forEach(FileUtil::deleteFile); // 删除文件
        }
    }

    /**
     * 检查文件夹是否为空
     * 判断传入的文件夹是否包含文件或子文件夹。
     * @param files 文件夹中的文件数组
     * @return boolean 如果文件夹非空，返回 true；否则返回 false4
     */
    public static boolean isNotEmpty(File[] files) {
        return null != files && files.length > 0;
    }

    /**
     * 删除单个文件
     * 删除指定路径的文件。
     * @param filePath 文件路径
     */
    private static void deleteFile(Path filePath) {
        try {
            Files.delete(filePath); // 删除文件
        } catch (IOException e) {
            e.printStackTrace(); // 如果删除文件失败，打印异常信息
        }
    }
}

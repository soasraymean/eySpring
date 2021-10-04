package by.dkozyrev.storage;

import by.dkozyrev.domain.ConnectionClass;
import by.dkozyrev.domain.Record;
import by.dkozyrev.util.ExcelUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

//StorageService implementation class
@Service
public class FileSystemStorageService implements StorageService {

    private final Path rootLocation;
    private static final Connection CONNECTION = ConnectionClass.getInstance().getConnection();

    @Autowired
    public FileSystemStorageService(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation());
    }

    @Override
    public boolean store(MultipartFile file) {
        boolean f = true;
        try {
            if (file.isEmpty()) {
                f = false;
                throw new StorageException("Failed to store empty file.");
            }
            if (!file.getOriginalFilename().contains(".xls") && !file.getOriginalFilename().contains("xlsx")) {
                f = false;
                throw new StorageException("Failed to store file of this type.");
            }

            File tmpFile = new File("src/main/resources/file.xls");
            OutputStream os = new FileOutputStream(tmpFile);
            os.write(file.getBytes());
            ExcelUtil.getInstance().excelToHtml(file.getOriginalFilename(), tmpFile);

            FileInputStream fileInputStream = new FileInputStream(tmpFile);
            HSSFWorkbook workbook = new HSSFWorkbook(fileInputStream);
            HSSFSheet sheet = workbook.getSheetAt(0);

            List<Record> recordList = new ArrayList<>();
            for (int i = 9; i < sheet.getLastRowNum(); i++) {

                HSSFRow row = sheet.getRow(i);

                if (row.getCell(0).toString().length()<=9) {
                    Record record = new Record();
                    int fieldCounter = 0;
                    for (int j = 0; j < row.getLastCellNum(); j++) {
                        if (fieldCounter >= 7) {
                            break;
                        }

                        CellType type = row.getCell(j).getCellType();
                        if (type == CellType.STRING) {
                            String chk = row.getCell(0).getStringCellValue();
                            if (chk.length() > 9) {
                                break;
                            }
                        }
                        DecimalFormat decimalFormat = new DecimalFormat("#.##");

                        switch (j) {
                            case 0:
                                if (type == CellType.NUMERIC) {
                                    record.setBankNumber(Long.toString((long) row.getCell(0).getNumericCellValue()));
                                } else {
                                    record.setBankNumber(row.getCell(0).getStringCellValue());
                                }
                                fieldCounter++;
                                break;
                            case 1:
                                if (type == CellType.NUMERIC) {
                                    record.setIncomingSaldoActive(Double.parseDouble(decimalFormat.format(row.getCell(1).getNumericCellValue())));
                                } else {
                                    record.setIncomingSaldoActive(Double.parseDouble(decimalFormat.format(Double.parseDouble(row.getCell(1).getStringCellValue()))));
                                }
                                fieldCounter++;
                                break;
                            case 2:
                                if (type == CellType.NUMERIC) {

                                    record.setIncomingSaldoPassive(Double.parseDouble(decimalFormat.format(row.getCell(2).getNumericCellValue())));
                                } else {
                                    record.setIncomingSaldoPassive(Double.parseDouble(decimalFormat.format(Double.parseDouble(row.getCell(2).getStringCellValue()))));
                                }
                                fieldCounter++;
                                break;
                            case 3:
                                if (type == CellType.NUMERIC) {
                                    record.setDebet(Double.parseDouble(decimalFormat.format(row.getCell(3).getNumericCellValue())));
                                } else {
                                    record.setDebet(Double.parseDouble(decimalFormat.format(Double.parseDouble(row.getCell(3).getStringCellValue()))));
                                }
                                fieldCounter++;
                                break;
                            case 4:
                                if (type == CellType.NUMERIC) {
                                    record.setCredit(Double.parseDouble(decimalFormat.format(row.getCell(4).getNumericCellValue())));
                                } else {
                                    record.setCredit(Double.parseDouble(decimalFormat.format(Double.parseDouble(row.getCell(4).getStringCellValue()))));
                                }
                                fieldCounter++;
                                break;
                            case 5:
                                if (type == CellType.NUMERIC) {
                                    record.setOutcomingSaldoActive(Double.parseDouble(decimalFormat.format(row.getCell(5).getNumericCellValue())));
                                } else {
                                    record.setOutcomingSaldoActive(Double.parseDouble(decimalFormat.format(Double.parseDouble(row.getCell(5).getStringCellValue()))));
                                }
                                fieldCounter++;
                                break;
                            case 6:
                                if (type == CellType.NUMERIC) {
                                    record.setOutcomingSaldoPassive(Double.parseDouble(decimalFormat.format(row.getCell(6).getNumericCellValue())));
                                } else {
                                    record.setOutcomingSaldoPassive(Double.parseDouble(decimalFormat.format(Double.parseDouble(row.getCell(6).getStringCellValue()))));
                                }
                                fieldCounter++;
                                break;
                        }
                    }
                    if (record.getIncomingSaldoActive() != null) {
                        recordList.add(record);
                    }
                }
            }
            insertIntoDb(recordList);
            tmpFile.delete();
            Path destinationFile = this.rootLocation.resolve(
                    Paths.get(file.getOriginalFilename()))
                    .normalize().toAbsolutePath();
            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                // This is a security check
                throw new StorageException(
                        "Cannot store file outside current directory.");
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile,
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new StorageException("Failed to store file.", e);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return f;
    }

    private void insertIntoDb(List<Record> recordList) {
        for (Record record : recordList) {
            if (record.getBankNumber() == null ||
                    record.getIncomingSaldoActive() == null ||
                    record.getIncomingSaldoPassive() == null ||
                    record.getDebet() == null ||
                    record.getCredit() == null ||
                    record.getOutcomingSaldoActive() == null ||
                    record.getOutcomingSaldoPassive() == null) {

            } else
                try {
                    PreparedStatement preparedStatement = CONNECTION.prepareStatement("insert into bank_number(bank_num) values(?)");
                    preparedStatement.setString(1, record.getBankNumber());
                    preparedStatement.executeUpdate();

                    preparedStatement = CONNECTION.prepareStatement("insert into incoming_saldo(active, passive) values(?, ?)");
                    preparedStatement.setDouble(1, record.getIncomingSaldoActive());
                    preparedStatement.setDouble(2, record.getIncomingSaldoPassive());
                    preparedStatement.executeUpdate();

                    preparedStatement = CONNECTION.prepareStatement("insert into outcoming_saldo(active, passive) values(?, ?)");
                    preparedStatement.setDouble(1, record.getOutcomingSaldoActive());
                    preparedStatement.setDouble(2, record.getOutcomingSaldoPassive());
                    preparedStatement.executeUpdate();

                    preparedStatement = CONNECTION.prepareStatement("insert into rotation(debet, credit) values(?, ?)");
                    preparedStatement.setDouble(1, record.getDebet());
                    preparedStatement.setDouble(2, record.getCredit());
                    preparedStatement.executeUpdate();

                } catch (SQLException exception) {
                    exception.printStackTrace();
                }
        }

    }


    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.rootLocation, 1)
                    .filter(path -> !path.equals(this.rootLocation))
                    .map(this.rootLocation::relativize);
        } catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }

    }

    @Override
    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException(
                        "Could not read file: " + filename);

            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }
}

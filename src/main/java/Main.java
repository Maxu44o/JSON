import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
        //Дано
        String person1 = "1,John,Smith,USA,25";
        String person2 = "2,Ivan,Petrov,RU,23";
        String csvfileName = "data.csv";
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};

        //Создаем csv
        try (CSVWriter writer = new CSVWriter(new FileWriter(csvfileName))) {
            writer.writeNext(person1.split(","));
            writer.writeNext(person2.split(","));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //CSV->JSON->File
        List<Employee> staff1 = parseSCV(columnMapping, csvfileName);
        String json1 = listToJson(staff1);
        writeString("data.json", json1);

        //XML->JSON->File
        List<Employee> staff2 = parseXML("data2.xml");
        String json2 = listToJson(staff1);
        writeString("data2.json", json2);
    }

    private static List<Employee> parseXML(String s) throws ParserConfigurationException, IOException, SAXException {

        List<Employee> stafflist = new ArrayList<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new File(s));

        NodeList employeeElements = document.getDocumentElement().getElementsByTagName("employee");

        // Перебор всех элементов employee
        for (int i = 0; i < employeeElements.getLength(); i++) {
            Node employee = employeeElements.item(i);
            NodeList empData = employee.getChildNodes();

            long id = 0;
            String firstname = null;
            String lastname = null;
            String country = null;
            int age = 0;
            // Перебор всех араметров каждого employee
            for (int j = 0; j < empData.getLength(); j++) {
                Node cNode = empData.item(j);
                if (empData.item(j) instanceof Element) {
                    String content = empData.item(j).getLastChild().getTextContent().trim();
                    switch (cNode.getNodeName()) {
                        case "id":
                            id = Long.parseLong(content);
                            break;
                        case "firstName":
                            firstname = content;
                            break;
                        case "lastName":
                            lastname = content;
                            break;
                        case "country":
                            country = content;
                            break;
                        case "age":
                            age = Integer.parseInt(content);
                            break;
                    }
                }
            }
            Employee tmpEmp = new Employee(id, firstname, lastname, country, age);
            stafflist.add(tmpEmp);
        }
        return stafflist;
    }

    private static void writeString(String filename, String content) {
        try (FileWriter fr = new FileWriter(filename)) {
            fr.write(content);
            fr.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static String listToJson(List<Employee> list) {
        GsonBuilder gbuilder = new GsonBuilder();
        Gson gson = gbuilder.create();
        return gson.toJson(list);
    }


    private static List<Employee> parseSCV(String[] columnMapping, String fileName) {
        List<Employee> staff = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new FileReader(fileName))) {
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(columnMapping);
            CsvToBean<Employee> objBuilder = new CsvToBeanBuilder<Employee>(csvReader).withMappingStrategy(strategy).build();
            staff = objBuilder.parse().stream().collect(Collectors.toList());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return staff;
    }


}

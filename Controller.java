package sample;

import com.jfoenix.controls.*;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.LocalDate;
import java.util.*;

public class Controller {
    @FXML MaterialDesignIconView closeIcon;
    @FXML JFXListView<JFXButton> boardList = new JFXListView<>();

    @FXML JFXTextField nameField;
    @FXML JFXTextField modelField;
    @FXML JFXTextField positionField;
    @FXML JFXTextArea descriptionArea;
    @FXML JFXTextField idField;
    @FXML JFXToggleButton originalToggle;
    @FXML JFXDatePicker orderDate;
    @FXML Label warningDialog;


    @FXML JFXButton saveButton;
    @FXML JFXButton updateButton;
    @FXML JFXButton readButton;
    @FXML JFXButton editButton;
    @FXML JFXButton eraseButton;




    private Integer position;

    List<Product> productList= new ArrayList<>();


    public void closeApp()
    {
        System.exit(0);
    }


    public String fillString(String x,Integer size)
    {
        while(x.length()<size)
            x+="|";
        return x;
    }

    public String chop(String x)
    {
        String chopped="";
        int i=0;
        while(x.charAt(i)!='|')
            chopped+=x.charAt(i++);
        return chopped;
    }

    public void updateProductList()
    {
        try{
            productList.clear();
            boardList.getItems().clear();
            Product aux1= new Product();
            RandomAccessFile file= new RandomAccessFile("products.txt","r");
            for(Integer i=0;i<file.length()/aux1.getProductSize();i++)
            {
                Product aux= new Product();
                //Set pos
                aux.setPosition(i);

                //Set ID
                byte[] buffer= new byte[aux.getIdSize()];
                file.read(buffer);
                aux.setId(Integer.parseInt( chop(new String(buffer))) );

                //Set name
                buffer= new byte[aux.getNameSize()];
                file.read(buffer);
                aux.setName( chop(new String(buffer)) );

                //Set model
                buffer= new byte[aux.getModelSize()];
                file.read(buffer);
                aux.setModel( chop(new String(buffer))  );

                //Set original bool value
                buffer= new byte[aux.getOriginalSize()];
                file.read(buffer);
                if(new String(buffer)=="1"){
                    aux.setOriginal(true);
                } else aux.setOriginal(false);

                //Set description field
                buffer = new byte[aux.getDescriptionSize()];
                file.read(buffer);
                aux.setDescription( chop(new String(buffer)) );

                //Set date
                buffer= new byte[aux.getDateSize()];
                file.read(buffer);
                LocalDate ld= LocalDate.parse(new String(buffer));
                aux.setDate(ld);

                productList.add(aux);
                boardList.getItems().
                        add(new JFXButton(i.toString()+"-"+aux.getName() +"--ID: "+
                                aux.getId().toString()));

            }
            file.close();
        } catch(IOException e){e.printStackTrace();}
    }

    public void newProduct()
    {
        String strOriginal="0";
        String name= nameField.getText();
        String model= modelField.getText();
        String description=descriptionArea.getText();
        String id=idField.getText();
        Boolean original= originalToggle.isSelected();
        LocalDate date= orderDate.getValue();
        Boolean noIdProblem=true;
        for(Product x: productList)
        {
            if(x.id==Integer.parseInt(id)) noIdProblem=false;
        }
        if(noIdProblem) {
            warningDialog.setVisible(false);
            name = fillString(name, 30);
            model = fillString(model, 30);
            description = fillString(description, 100);
            id = fillString(id, 10);
            String strDate = date.toString();
            if (strDate.length() == 0) strDate = LocalDate.now().toString();
            if (original) strOriginal = "1";

            try {
                RandomAccessFile file = new RandomAccessFile("products.txt", "rw");
                long fileLength = file.length();
                file.seek(fileLength);

                System.out.println(fileLength);

                file.writeBytes(id);
                file.writeBytes(name);
                file.writeBytes(model);
                file.writeBytes(strOriginal);
                file.writeBytes(description);
                file.writeBytes(strDate);

                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            warningDialog.setVisible(true);
        }
    }

    public void updateProduct()
    {

        String strOriginal="0";
        String name= nameField.getText();
        String model= modelField.getText();
        String description=descriptionArea.getText();
        String id=idField.getText();
        Boolean original= originalToggle.isSelected();
        LocalDate date= orderDate.getValue();
        Boolean noIdProblem=true;
        Product aux= new Product();


        name = fillString(name, 30);
        model = fillString(model, 30);
        description = fillString(description, 100);
        id = fillString(id, 10);
        String strDate = date.toString();
        if (strDate.length() == 0) strDate = LocalDate.now().toString();
        if (original) strOriginal = "1";

        position=Integer.parseInt(positionField.getText());
        Integer actualId=productList.get(position).getId();
        System.out.println(actualId);
        for(Product x: productList)
        {
            if(x.id==Integer.parseInt(chop(id))) noIdProblem=false;
            if(x.id==actualId)noIdProblem=true;
            System.out.println(x.name);
            System.out.println(x.id);
        }

        if(noIdProblem) {
            warningDialog.setVisible(false);
            try {
                RandomAccessFile file = new RandomAccessFile("products.txt", "rw");
                file.seek(position * aux.getProductSize());
                file.writeBytes(id);
                file.writeBytes(name);
                file.writeBytes(model);
                file.writeBytes(strOriginal);
                file.writeBytes(description);
                file.writeBytes(strDate);
                file.close();
                updateProductList();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else warningDialog.setVisible(true);

    }

    public void eraseProduct()
    {
        Integer pos= Integer.parseInt(positionField.getText());
        try
        {
            RandomAccessFile file= new RandomAccessFile("products.txt","rw");
            RandomAccessFile backup= new RandomAccessFile("back.up","rw");
            Product aux= new Product();
            for(Integer i=0;i<file.length();i++) {
                if (file.getFilePointer() / aux.getProductSize() == pos) {
                    file.seek(file.getFilePointer() + aux.getProductSize());
                    i = i + aux.getProductSize()-1;
                } else
                    backup.write(file.read());
            }
            file.close();
            backup.close();

            File rawFile= new File("products.txt");
            rawFile.delete();

            file= new RandomAccessFile("products.txt","rw");
            backup= new RandomAccessFile("back.up","rw");

            for(Integer i=0;i<backup.length();i++)
                file.write(backup.read());
            file.close();
            backup.close();

            rawFile= new File("back.up");
            rawFile.delete();

        } catch (IOException e){e.printStackTrace();}
    }

    public void showProduct()
    {
        position=Integer.parseInt(positionField.getText());
        try
        {
            Product aux= new Product();
            RandomAccessFile file= new
                    RandomAccessFile("products.txt","r");

            Long pos= Long.parseLong(positionField.getText());
            file.seek(pos*aux.getProductSize());

            //Set ID in field
            byte[] buffer= new byte[aux.getIdSize()];
            file.read(buffer);
            aux.setId(Integer.parseInt( chop(new String(buffer))) );

            //Set name in field
            buffer= new byte[aux.getNameSize()];
            file.read(buffer);
            aux.setName( chop(new String(buffer)) );

            //Set model in field
            buffer= new byte[aux.getModelSize()];
            file.read(buffer);
            aux.setModel( chop(new String(buffer))  );

            //Set original bool value in toggle
            buffer= new byte[aux.getOriginalSize()];
            file.read(buffer);
            String x=new String(buffer);
            if(x.compareTo("1")==0){
                aux.setOriginal(true);
            } else aux.setOriginal(false);

            //Set description field
            buffer = new byte[aux.getDescriptionSize()];
            file.read(buffer);
            aux.setDescription( chop(new String(buffer)) );

            //Set date in datePicker
            buffer= new byte[aux.getDateSize()];
            file.read(buffer);
            LocalDate ld= LocalDate.parse(new String(buffer));
            aux.setDate(ld);

            idField.setText(aux.getId().toString());
            orderDate.setValue(aux.getDate());
            nameField.setText(aux.getName());
            modelField.setText(aux.getModel());
            descriptionArea.setText(aux.getDescription());
            if(aux.getOriginal()==true)
            {
                originalToggle.setSelected(true);
            } else originalToggle.setSelected(false);

            file.close();
        } catch (IOException e){e.printStackTrace();}
    }

    public void initialize()
    {
        updateProductList();
    }


    public class Product {
        Integer id;
        Integer position;
        LocalDate date;
        String name;
        String description;
        String model;
        Boolean original;

        public Product(Integer id, Integer position, LocalDate date, String name,
                       String description, String model, Boolean original) {
            this.id = id;
            this.position = position;
            this.date = date;
            this.name = name;
            this.description = description;
            this.model = model;
            this.original = original;
        }

        public Product(){}

        public Integer getProductSize(){return 181;}
        public Integer getIdSize() {return 10;}
        public Integer getNameSize() { return 30;}
        public Integer getModelSize(){return 30;}
        public Integer getOriginalSize(){return 1;}
        public Integer getDescriptionSize(){return 100;}
        public Integer getDateSize(){return LocalDate.now().toString().length();}

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public Integer getPosition() {
            return position;
        }

        public void setPosition(Integer position) {
            this.position = position;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public Boolean getOriginal() {
            return original;
        }

        public void setOriginal(Boolean original) {
            this.original = original;
        }
    }

}

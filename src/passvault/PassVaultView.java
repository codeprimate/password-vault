/*
 * PassVaultView.java
 */

package passvault;

import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import passvault.resources.Credential;
import passvault.resources.PasswordStore;
import passvault.resources.Server;
import passvault.resources.Person;
import passvault.resources.ExtensionFileFilter;

import javax.swing.filechooser.FileFilter;
import passvault.resources.PasswordCheck;
import passvault.resources.PasswordStoreSearch;
import passvault.resources.StoreReader;
import passvault.resources.StoreWriter;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javax.swing.JOptionPane;

/**
 * The application's main frame.
 */
public class PassVaultView extends FrameView {

    public PassVaultView(SingleFrameApplication app) {
        super(app);

        initComponents();
        

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String)(evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer)(evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
    }

    @Action
    public void showAboutBox(ActionEvent e) {
        if (aboutBox == null) {
            JFrame mainFrame = PassVaultApp.getApplication().getMainFrame();
            aboutBox = new PassVaultAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        PassVaultApp.getApplication().show(aboutBox);
    }
   

    public void save_credential() {
        if (cred_descField.getText().equals("")) {
            statusMessageLabel.setText("Please enter a description!");
            return;
        }

        String server_name = (String)cred_serverCombo.getSelectedItem();
        String person_name = (String)cred_personCombo.getSelectedItem();

        
        if (server_name != null) {
            String server_server_name = server_nameField.getText();
            if (server_name.equals(server_server_name)) {
                save_server();
            }
        }
        
        if (person_name != null) {
            String person_person_name = person_nameField.getText();
            if (person_name.equals(person_person_name)) {
                save_person();
            }
        }
        
        PasswordStore pws = PassVaultApp.getApplication().passwordStore;
        Credential new_cred = new Credential();
        new_cred.description = cred_descField.getText();
        
        
        new_cred.server = pws.add_server(server_name);
        new_cred.person = pws.add_person(person_name);
        new_cred.address = cred_addressField.getText();
        new_cred.login = cred_loginField.getText();
        new_cred.password = cred_passwordField.getText();
        new_cred.notes = cred_notesField.getText();
        
        pws.update_credential(new_cred);
        
        clear_credential();
        populate_tree();
        browseTree.expandRow(1);
        
        statusMessageLabel.setText("Created Credential");
    }
    
    public String[] get_server_names() {
        PasswordStore pws = PassVaultApp.getApplication().passwordStore;
        return pws.server_names();
    }
    
    public String[] get_person_names() {
        PasswordStore pws = PassVaultApp.getApplication().passwordStore;
        return pws.person_names();
    }
    
    public String[] get_credential_names() {
        PasswordStore pws = PassVaultApp.getApplication().passwordStore;
        return pws.credential_names();
    }
    
    public void populate_server_list(String server_name) {
        PasswordStore pws = PassVaultApp.getApplication().passwordStore;
        cred_serverCombo.removeAllItems();
        cred_serverCombo.addItem(server_name);
        String [] server_names = pws.server_names();
        for (int i = 0; i < server_names.length; i++) {
            cred_serverCombo.addItem(server_names[i]);
        }
    }
    
    public void populate_server_list(){
        PasswordStore pws = PassVaultApp.getApplication().passwordStore;
        cred_serverCombo.removeAllItems();
        String [] server_names = pws.server_names();
        for (int i = 0; i < server_names.length; i++) {
            cred_serverCombo.addItem(server_names[i]);
        }
    }
    
    public void populate_name_list(){
        PasswordStore pws = PassVaultApp.getApplication().passwordStore;
        cred_personCombo.removeAllItems();
        String [] person_names = pws.person_names();
        for (int i = 0; i < person_names.length; i++) {
            cred_personCombo.addItem(person_names[i]);
        }
    }
    
    public void populate_name_list(String person_name){
        PasswordStore pws = PassVaultApp.getApplication().passwordStore;
        cred_personCombo.removeAllItems();
        cred_personCombo.addItem(person_name);
        String [] person_names = pws.person_names();
        for (int i = 0; i < person_names.length; i++) {
            cred_personCombo.addItem(person_names[i]);
        }
    }
    
    private DefaultMutableTreeNode populate_tree_servers_node(DefaultMutableTreeNode root, ArrayList<Server> servers) {
      DefaultMutableTreeNode servers_node = new DefaultMutableTreeNode("Servers");
       servers_node.setAllowsChildren(true);
       for (int i = 0; i < servers.size(); i++) {
           Server server = servers.get(i);
           if (server == null) {
//               return;
           }
           DefaultMutableTreeNode child_node = new DefaultMutableTreeNode(server);
           servers_node.add(child_node);
       }
        return servers_node;
    }
    
    private DefaultMutableTreeNode populate_tree_people_node(DefaultMutableTreeNode root, ArrayList<Person> people) {
       DefaultMutableTreeNode people_node = new DefaultMutableTreeNode("People");
       people_node.setAllowsChildren(true);
       for (int i = 0; i < people.size(); i++) {
           Person person = people.get(i);
           DefaultMutableTreeNode child_node = new DefaultMutableTreeNode(person);
           people_node.add(child_node);
       } 
        return people_node;
    }
    
    private DefaultMutableTreeNode populate_tree_credentials_node(DefaultMutableTreeNode root, ArrayList<Credential> credentials) {
       DefaultMutableTreeNode credentials_node = new DefaultMutableTreeNode("Credentials");
       credentials_node.setAllowsChildren(true);
       for (int i = 0; i < credentials.size(); i++) {
           Credential credential = credentials.get(i);
           DefaultMutableTreeNode child_node = new DefaultMutableTreeNode(credential);
           credentials_node.add(child_node);
       }
        return credentials_node;
    }
    
    public void populate_tree(){ 
       PasswordStore pws = PassVaultApp.getApplication().passwordStore;
       DefaultMutableTreeNode root = new DefaultMutableTreeNode("Browse");
       DefaultTreeModel model = new javax.swing.tree.DefaultTreeModel(root);
       
       DefaultMutableTreeNode servers_node = populate_tree_servers_node(root, pws.servers);
       model.insertNodeInto(servers_node, root, 0); 
       
       DefaultMutableTreeNode people_node = populate_tree_people_node(root, pws.people);
       model.insertNodeInto(people_node, root, 0);
       
       DefaultMutableTreeNode credentials_node = populate_tree_credentials_node(root, pws.credentials);
       model.insertNodeInto(credentials_node, root, 0);

       browseTree.setModel(model);
       
       browseTree.getSelectionModel().setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
       expandTree();
    }
    
    public void populate_tree(PasswordStoreSearch pwss) {
       DefaultMutableTreeNode root = new DefaultMutableTreeNode("Browse");
       DefaultTreeModel model = new javax.swing.tree.DefaultTreeModel(root);
       
       DefaultMutableTreeNode servers_node = populate_tree_servers_node(root, pwss.servers);
       model.insertNodeInto(servers_node, root, 0); 
       
       DefaultMutableTreeNode people_node = populate_tree_people_node(root, pwss.people);
       model.insertNodeInto(people_node, root, 0);
       
       DefaultMutableTreeNode credentials_node = populate_tree_credentials_node(root, pwss.credentials);
       model.insertNodeInto(credentials_node, root, 0);

       browseTree.setModel(model);
       
       browseTree.getSelectionModel().setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
       expandTree();
    }
    
    public PasswordStore get_pws(){
        PasswordStore pws = PassVaultApp.getApplication().passwordStore;
        return pws;
    }
    
    public void initialize_display(){
        clear_credential();
        populate_tree();
    }
    
    private void startup_methods(){
        open_default_store();
        initialize_display();
    }
    
    public void clear_credential() {
        cred_descField.setText("");
        cred_addressField.setText("");
        cred_loginField.setText("");
        cred_passwordField.setText("");
        cred_notesField.setText("");
        populate_server_list();
        populate_name_list();
        cred_descField.grabFocus();
        statusMessageLabel.setText("");
    
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        browseTree = new javax.swing.JTree();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        credPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        cred_serverCombo = new javax.swing.JComboBox(get_server_names());
        jLabel2 = new javax.swing.JLabel();
        cred_personCombo = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        cred_loginField = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        cred_passwordField = new javax.swing.JTextField();
        cred_genpassButton = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        cred_notesField = new javax.swing.JTextArea();
        clearentryButton = new javax.swing.JButton();
        saveentryButton = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        cred_addressField = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        cred_descField = new javax.swing.JTextField();
        delete_credButton = new javax.swing.JButton();
        strengthBar = new javax.swing.JProgressBar();
        passwordVerdict = new javax.swing.JLabel();
        serverPanel = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        server_nameField = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        server_addressField = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        server_notesField = new javax.swing.JTextArea();
        saveserverButton = new javax.swing.JButton();
        clearserverButton = new javax.swing.JButton();
        delete_serverButton = new javax.swing.JButton();
        personPanel = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        person_nameField = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        person_emailField = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        person_notesField = new javax.swing.JTextArea();
        savepersonButton = new javax.swing.JButton();
        clearpersonButton = new javax.swing.JButton();
        delete_personButton = new javax.swing.JButton();
        refreshTreeButton = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem openItem = new javax.swing.JMenuItem();
        saveItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        openDialog = new javax.swing.JDialog();
        fileChooser = new javax.swing.JFileChooser();

        mainPanel.setName("mainPanel"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(passvault.PassVaultApp.class).getContext().getResourceMap(PassVaultView.class);
        browseTree.setBackground(resourceMap.getColor("browseTree.background")); // NOI18N
        browseTree.setName("browseTree"); // NOI18N
        browseTree.setScrollsOnExpand(false);
        browseTree.setToggleClickCount(1);
        browseTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                browseTreeValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(browseTree);

        jTabbedPane1.setDoubleBuffered(true);
        jTabbedPane1.setName("jTabbedPane1"); // NOI18N
        jTabbedPane1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTabbedPane1FocusGained(evt);
            }
        });

        credPanel.setName("credPanel"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        cred_serverCombo.setEditable(true);
        cred_serverCombo.setName("cred_serverCombo"); // NOI18N
        cred_serverCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cred_serverComboItemStateChanged(evt);
            }
        });
        cred_serverCombo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                cred_serverComboFocusLost(evt);
            }
        });
        cred_serverCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                cred_serverComboKeyTyped(evt);
            }
        });

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        cred_personCombo.setEditable(true);
        cred_personCombo.setName("cred_personCombo"); // NOI18N
        cred_personCombo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                cred_personComboFocusLost(evt);
            }
        });

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        cred_loginField.setText(resourceMap.getString("cred_loginField.text")); // NOI18N
        cred_loginField.setName("cred_loginField"); // NOI18N
        cred_loginField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cred_loginFieldActionPerformed(evt);
            }
        });

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        cred_passwordField.setFont(resourceMap.getFont("cred_passwordField.font")); // NOI18N
        cred_passwordField.setText(resourceMap.getString("cred_passwordField.text")); // NOI18N
        cred_passwordField.setName("cred_passwordField"); // NOI18N
        cred_passwordField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                cred_passwordFieldKeyTyped(evt);
            }
        });

        cred_genpassButton.setText(resourceMap.getString("cred_genpassButton.text")); // NOI18N
        cred_genpassButton.setName("cred_genpassButton"); // NOI18N
        cred_genpassButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cred_genpassButtonActionPerformed(evt);
            }
        });

        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        cred_notesField.setColumns(20);
        cred_notesField.setLineWrap(true);
        cred_notesField.setRows(5);
        cred_notesField.setWrapStyleWord(true);
        cred_notesField.setName("cred_notesField"); // NOI18N
        jScrollPane2.setViewportView(cred_notesField);

        clearentryButton.setText(resourceMap.getString("clearentryButton.text")); // NOI18N
        clearentryButton.setName("clearentryButton"); // NOI18N
        clearentryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearentryButtonActionPerformed(evt);
            }
        });

        saveentryButton.setText(resourceMap.getString("saveentryButton.text")); // NOI18N
        saveentryButton.setName("saveentryButton"); // NOI18N
        saveentryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveentryButtonActionPerformed(evt);
            }
        });

        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        cred_addressField.setText(resourceMap.getString("cred_addressField.text")); // NOI18N
        cred_addressField.setName("cred_addressField"); // NOI18N

        jLabel13.setText(resourceMap.getString("jLabel13.text")); // NOI18N
        jLabel13.setName("jLabel13"); // NOI18N

        cred_descField.setText(resourceMap.getString("cred_descField.text")); // NOI18N
        cred_descField.setName("cred_descField"); // NOI18N

        delete_credButton.setText(resourceMap.getString("delete_credButton.text")); // NOI18N
        delete_credButton.setName("delete_credButton"); // NOI18N
        delete_credButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delete_credButtonActionPerformed(evt);
            }
        });

        strengthBar.setName("strengthBar"); // NOI18N

        passwordVerdict.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        passwordVerdict.setText(resourceMap.getString("passwordVerdict.text")); // NOI18N
        passwordVerdict.setName("passwordVerdict"); // NOI18N

        org.jdesktop.layout.GroupLayout credPanelLayout = new org.jdesktop.layout.GroupLayout(credPanel);
        credPanel.setLayout(credPanelLayout);
        credPanelLayout.setHorizontalGroup(
            credPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(credPanelLayout.createSequentialGroup()
                .add(credPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(credPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(credPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(credPanelLayout.createSequentialGroup()
                                .add(84, 84, 84)
                                .add(cred_descField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE)
                                .add(59, 59, 59))
                            .add(jLabel13)))
                    .add(delete_credButton)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, credPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(credPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(credPanelLayout.createSequentialGroup()
                                .add(60, 60, 60)
                                .add(saveentryButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(clearentryButton))
                            .add(credPanelLayout.createSequentialGroup()
                                .add(credPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jLabel1)
                                    .add(jLabel2)
                                    .add(jLabel6)
                                    .add(jLabel3)
                                    .add(jLabel4))
                                .add(21, 21, 21)
                                .add(credPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, credPanelLayout.createSequentialGroup()
                                        .add(credPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                            .add(credPanelLayout.createSequentialGroup()
                                                .add(strengthBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                                            .add(org.jdesktop.layout.GroupLayout.LEADING, credPanelLayout.createSequentialGroup()
                                                .add(passwordVerdict, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 143, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                .add(10, 10, 10)))
                                        .add(cred_genpassButton)
                                        .add(2, 2, 2))
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, credPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                        .add(cred_passwordField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE)
                                        .add(org.jdesktop.layout.GroupLayout.TRAILING, cred_loginField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE)
                                        .add(org.jdesktop.layout.GroupLayout.TRAILING, cred_personCombo, 0, 301, Short.MAX_VALUE)
                                        .add(org.jdesktop.layout.GroupLayout.TRAILING, cred_serverCombo, 0, 301, Short.MAX_VALUE)
                                        .add(cred_addressField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE)))
                                .add(59, 59, 59))))
                    .add(credPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLabel5)
                        .add(19, 19, 19)
                        .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 384, Short.MAX_VALUE)))
                .addContainerGap())
        );
        credPanelLayout.setVerticalGroup(
            credPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, credPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(credPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel13)
                    .add(cred_descField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(credPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(cred_serverCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(credPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel6)
                    .add(cred_addressField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(credPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(cred_personCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(credPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(cred_loginField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(credPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cred_passwordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel4))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(credPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(cred_genpassButton)
                    .add(credPanelLayout.createSequentialGroup()
                        .add(strengthBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(3, 3, 3)
                        .add(passwordVerdict)))
                .add(18, 18, 18)
                .add(credPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel5)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(credPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(clearentryButton)
                    .add(saveentryButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(delete_credButton)
                .add(8, 8, 8))
        );

        jTabbedPane1.addTab(resourceMap.getString("credPanel.TabConstraints.tabTitle"), credPanel); // NOI18N

        serverPanel.setName("serverPanel"); // NOI18N
        serverPanel.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                serverPanelFocusGained(evt);
            }
        });

        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        server_nameField.setText(resourceMap.getString("server_nameField.text")); // NOI18N
        server_nameField.setName("server_nameField"); // NOI18N

        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        server_addressField.setText(resourceMap.getString("server_addressField.text")); // NOI18N
        server_addressField.setName("server_addressField"); // NOI18N

        jLabel9.setText(resourceMap.getString("jLabel9.text")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        server_notesField.setColumns(20);
        server_notesField.setLineWrap(true);
        server_notesField.setRows(5);
        server_notesField.setWrapStyleWord(true);
        server_notesField.setName("server_notesField"); // NOI18N
        jScrollPane3.setViewportView(server_notesField);

        saveserverButton.setText(resourceMap.getString("saveserverButton.text")); // NOI18N
        saveserverButton.setName("saveserverButton"); // NOI18N
        saveserverButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveserverButtonActionPerformed(evt);
            }
        });

        clearserverButton.setText(resourceMap.getString("clearserverButton.text")); // NOI18N
        clearserverButton.setName("clearserverButton"); // NOI18N
        clearserverButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearserverButtonActionPerformed(evt);
            }
        });

        delete_serverButton.setText(resourceMap.getString("delete_serverButton.text")); // NOI18N
        delete_serverButton.setName("delete_serverButton"); // NOI18N
        delete_serverButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delete_serverButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout serverPanelLayout = new org.jdesktop.layout.GroupLayout(serverPanel);
        serverPanel.setLayout(serverPanelLayout);
        serverPanelLayout.setHorizontalGroup(
            serverPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(serverPanelLayout.createSequentialGroup()
                .add(serverPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(serverPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(serverPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel8)
                            .add(jLabel7)
                            .add(jLabel9))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(serverPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, serverPanelLayout.createSequentialGroup()
                                .add(saveserverButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 303, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(clearserverButton))
                            .add(server_addressField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, server_nameField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE)
                            .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE)))
                    .add(delete_serverButton))
                .addContainerGap())
        );
        serverPanelLayout.setVerticalGroup(
            serverPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(serverPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(serverPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel7)
                    .add(server_nameField))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(serverPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel8)
                    .add(server_addressField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(serverPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel9)
                    .add(jScrollPane3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(serverPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(clearserverButton)
                    .add(saveserverButton))
                .add(168, 168, 168)
                .add(delete_serverButton)
                .add(17, 17, 17))
        );

        jTabbedPane1.addTab(resourceMap.getString("serverPanel.TabConstraints.tabTitle"), serverPanel); // NOI18N

        personPanel.setName("personPanel"); // NOI18N
        personPanel.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                personPanelFocusGained(evt);
            }
        });

        jLabel10.setText(resourceMap.getString("jLabel10.text")); // NOI18N
        jLabel10.setName("jLabel10"); // NOI18N

        person_nameField.setText(resourceMap.getString("person_nameField.text")); // NOI18N
        person_nameField.setName("person_nameField"); // NOI18N

        jLabel11.setText(resourceMap.getString("jLabel11.text")); // NOI18N
        jLabel11.setName("jLabel11"); // NOI18N

        person_emailField.setText(resourceMap.getString("person_emailField.text")); // NOI18N
        person_emailField.setName("person_emailField"); // NOI18N

        jLabel12.setText(resourceMap.getString("jLabel12.text")); // NOI18N
        jLabel12.setName("jLabel12"); // NOI18N

        jScrollPane4.setName("jScrollPane4"); // NOI18N

        person_notesField.setColumns(20);
        person_notesField.setLineWrap(true);
        person_notesField.setRows(5);
        person_notesField.setWrapStyleWord(true);
        person_notesField.setName("person_notesField"); // NOI18N
        jScrollPane4.setViewportView(person_notesField);

        savepersonButton.setText(resourceMap.getString("savepersonButton.text")); // NOI18N
        savepersonButton.setName("savepersonButton"); // NOI18N
        savepersonButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                savepersonButtonActionPerformed(evt);
            }
        });

        clearpersonButton.setText(resourceMap.getString("clearpersonButton.text")); // NOI18N
        clearpersonButton.setName("clearpersonButton"); // NOI18N
        clearpersonButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearpersonButtonActionPerformed(evt);
            }
        });

        delete_personButton.setText(resourceMap.getString("delete_personButton.text")); // NOI18N
        delete_personButton.setName("delete_personButton"); // NOI18N
        delete_personButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delete_personButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout personPanelLayout = new org.jdesktop.layout.GroupLayout(personPanel);
        personPanel.setLayout(personPanelLayout);
        personPanelLayout.setHorizontalGroup(
            personPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(personPanelLayout.createSequentialGroup()
                .add(personPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(personPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(personPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel10)
                            .add(jLabel11)
                            .add(jLabel12))
                        .add(24, 24, 24)
                        .add(personPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, personPanelLayout.createSequentialGroup()
                                .add(savepersonButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 303, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(clearpersonButton))
                            .add(person_emailField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE)
                            .add(person_nameField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE)
                            .add(jScrollPane4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE)))
                    .add(delete_personButton))
                .addContainerGap())
        );
        personPanelLayout.setVerticalGroup(
            personPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(personPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(personPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel10)
                    .add(person_nameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(personPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel11)
                    .add(person_emailField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(17, 17, 17)
                .add(personPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel12)
                    .add(jScrollPane4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(personPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(clearpersonButton)
                    .add(savepersonButton))
                .add(166, 166, 166)
                .add(delete_personButton)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(resourceMap.getString("personPanel.TabConstraints.tabTitle"), personPanel); // NOI18N

        jTabbedPane1.setSelectedComponent(credPanel);

        refreshTreeButton.setText(resourceMap.getString("refreshTreeButton.text")); // NOI18N
        refreshTreeButton.setName("refreshTreeButton"); // NOI18N
        refreshTreeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshTreeButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout mainPanelLayout = new org.jdesktop.layout.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(refreshTreeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 209, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 208, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 504, Short.MAX_VALUE)
                .add(27, 27, 27))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mainPanelLayout.createSequentialGroup()
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(mainPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 418, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(refreshTreeButton))
                    .add(mainPanelLayout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 466, Short.MAX_VALUE)))
                .addContainerGap())
        );

        startup_methods();

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        openItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        openItem.setText(resourceMap.getString("openItem.text")); // NOI18N
        openItem.setName("openItem"); // NOI18N
        openItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openItemActionPerformed(evt);
            }
        });
        fileMenu.add(openItem);

        saveItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveItem.setText(resourceMap.getString("saveItem.text")); // NOI18N
        saveItem.setName("saveItem"); // NOI18N
        saveItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveItem);

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(passvault.PassVaultApp.class).getContext().getActionMap(PassVaultView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        org.jdesktop.layout.GroupLayout statusPanelLayout = new org.jdesktop.layout.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(statusPanelSeparator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 762, Short.MAX_VALUE)
            .add(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(statusMessageLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 566, Short.MAX_VALUE)
                .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(statusPanelLayout.createSequentialGroup()
                .add(statusPanelSeparator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(statusMessageLabel)
                    .add(statusAnimationLabel)
                    .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(3, 3, 3))
        );

        openDialog.setName("openDialog"); // NOI18N

        fileChooser.setCurrentDirectory(new java.io.File(get_path()));
        fileChooser.setName("fileChooser"); // NOI18N
        fileChooser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileChooserActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout openDialogLayout = new org.jdesktop.layout.GroupLayout(openDialog.getContentPane());
        openDialog.getContentPane().setLayout(openDialogLayout);
        openDialogLayout.setHorizontalGroup(
            openDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, openDialogLayout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(fileChooser, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        openDialogLayout.setVerticalGroup(
            openDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(openDialogLayout.createSequentialGroup()
                .addContainerGap()
                .add(fileChooser, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void cred_loginFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cred_loginFieldActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_cred_loginFieldActionPerformed

    private void saveentryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveentryButtonActionPerformed
        // TODO add your handling code here:
        save_server();
        save_person();
        save_credential();
    }//GEN-LAST:event_saveentryButtonActionPerformed

    private void clearentryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearentryButtonActionPerformed
        // TODO add your handling code here:
        clear_credential();
        update_password_strength_info();
    }//GEN-LAST:event_clearentryButtonActionPerformed

    private void cred_genpassButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cred_genpassButtonActionPerformed
        // TODO add your handling code here:
        cred_passwordField.setText(Credential.genPassword(12));
        update_password_strength_info();
    }//GEN-LAST:event_cred_genpassButtonActionPerformed

    private void populate_server_panel(Server server) {
        server_nameField.setText(server.name);
        server_addressField.setText(server.address);
        server_notesField.setText(server.notes);
//        String cred_addr = cred_addressField.getText();
//        String server_addr = server_addressField.getText();
//        if ((server_addr.length() == 0) && (cred_addr.length() > 0)) {
//            server_addressField.setText(cred_addr);
//        }
    }
    
    private void populate_person_panel(Person person) {
        person_nameField.setText(person.name);
        person_emailField.setText(person.email);
        person_notesField.setText(person.notes);
    }
    
    private void populate_credential_panel(Credential credential) {
        cred_descField.setText(credential.description);
        populate_server_list(credential.server.name);
        cred_addressField.setText(credential.address);
        populate_name_list(credential.person.name);
        cred_loginField.setText(credential.login);
        cred_passwordField.setText(credential.password);
        cred_notesField.setText(credential.notes);
        update_password_strength_info();
    }
    
    private void cred_serverComboFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_cred_serverComboFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_cred_serverComboFocusLost

    private void cred_personComboFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_cred_personComboFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_cred_personComboFocusLost

    private void cred_serverComboKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cred_serverComboKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_cred_serverComboKeyTyped

    private void jTabbedPane1FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTabbedPane1FocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event_jTabbedPane1FocusGained

    private void serverPanelFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_serverPanelFocusGained
        // TODO add your handling code here:
        PasswordStore pws = PassVaultApp.getApplication().passwordStore;
        String server_name = (String)cred_serverCombo.getSelectedItem();
        if (server_name.length() > 0) {
        Server server = pws.find_server(server_name);
            if (server != null) {
                populate_server_panel(server);
                if (cred_addressField.getText().length() == 0) {
                    cred_addressField.setText(server.address);
                }
            } 
            else {
                if ((server_nameField.getText().length() < 1) && (server_addressField.getText().length() < 1)) {
                    server_nameField.setText(server_name);
                    String server_address = cred_addressField.getText();
                    if (server_address.length() > 0) {
                        server_addressField.setText(server_address);
                    }
                }
            }
        }
}//GEN-LAST:event_serverPanelFocusGained

    private void personPanelFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_personPanelFocusGained
        // TODO add your handling code here:
        PasswordStore pws = PassVaultApp.getApplication().passwordStore;
        String person_name = (String)cred_personCombo.getSelectedItem();
        if (person_name.length() > 0) {
            Person person = pws.find_person(person_name);
            if (person != null) {
                populate_person_panel(person);
            }
            else {
                if ((person_emailField.getText().length() < 1) && (person_notesField.getText().length() < 1) ) {
                    person_nameField.setText(person_name);
                }
            }
        }
    }//GEN-LAST:event_personPanelFocusGained

    private void refreshTreeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshTreeButtonActionPerformed
        // TODO add your handling code here:
        populate_tree();
    }//GEN-LAST:event_refreshTreeButtonActionPerformed

    private void save_server() {
        Server server = new Server();
        server.name = server_nameField.getText();
        server.address = server_addressField.getText();
        server.notes = server_notesField.getText();
        if (server.name.length() > 0) {
            PasswordStore pws = PassVaultApp.getApplication().passwordStore;
            pws.update_server(server);
            statusMessageLabel.setText("Saved Server.");
        }
        else {
            statusMessageLabel.setText("Missing Server Name!");
            return;
        }
        populate_tree();
        browseTree.expandRow(3);
    }
    
    private void clear_server(){
        server_nameField.setText("");
        server_addressField.setText("");
        server_notesField.setText("");
        server_nameField.grabFocus();
    }
    
    private void clear_person(){
        person_nameField.setText("");
        person_emailField.setText("");
        person_notesField.setText("");
        person_nameField.grabFocus();
    }
    
    private void save_person() {
        Person person = new Person();
        person.name = person_nameField.getText();
        person.email = person_emailField.getText();
        person.notes = person_notesField.getText();
        if (person.name.length() > 0) {
            PasswordStore pws = PassVaultApp.getApplication().passwordStore;
            pws.update_person(person);
            statusMessageLabel.setText("Saved Person.");
        }
        else {
            statusMessageLabel.setText("Missing Person Name!");
            return;
        }
        populate_tree();
        browseTree.expandRow(2);
    }
    
    private void populate_cred_address_from_server() {
        String cred_address = cred_addressField.getText();
        Server server = null;
        String server_name = (String)cred_serverCombo.getSelectedItem();
        if (server_name != null) {
            PasswordStore pws = get_pws();
            server = pws.find_server(server_name);

        }
        if (server != null) {
            if (server.name.length() > 0) {
                cred_addressField.setText(server.address);
            }
        }
    }
    
    private void saveserverButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveserverButtonActionPerformed
        // TODO add your handling code here:
        save_server();
    }//GEN-LAST:event_saveserverButtonActionPerformed

    private void clearserverButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearserverButtonActionPerformed
        // TODO add your handling code here:
        clear_server();
    }//GEN-LAST:event_clearserverButtonActionPerformed

    private void savepersonButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_savepersonButtonActionPerformed
        // TODO add your handling code here:
        save_person();
    }//GEN-LAST:event_savepersonButtonActionPerformed

    private void clearpersonButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearpersonButtonActionPerformed
        // TODO add your handling code here:
        clear_person();
    }//GEN-LAST:event_clearpersonButtonActionPerformed

    private void cred_serverComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cred_serverComboItemStateChanged
        // TODO add your handling code here:      
        populate_cred_address_from_server();
    }//GEN-LAST:event_cred_serverComboItemStateChanged

    private void clear_panels(){
        clear_server();
        clear_credential();
        clear_person();
    }
    
    private void browseTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_browseTreeValueChanged
        // TODO add your handling code here:
        PasswordStore pws = get_pws();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)browseTree.getLastSelectedPathComponent();
        if (node == null) { return; }
        Object nodeInfo = node.getUserObject();
        if (node.isLeaf()) {
            Class node_class = nodeInfo.getClass();
            String classname = node_class.getSimpleName();
            if (classname.equals("Person")) {
                Person person = (Person)node.getUserObject();
                clear_panels();
                PasswordStoreSearch pwss = new PasswordStoreSearch(pws, person);
                populate_tree(pwss);
                populate_person_panel(person);
                jTabbedPane1.setSelectedComponent(personPanel);
                person_nameField.grabFocus();
            }
            if (classname.equals("Server")) {
                Server server = (Server)node.getUserObject();
                clear_panels();
                PasswordStoreSearch pwss = new PasswordStoreSearch(pws, server);
                populate_tree(pwss);
                populate_server_panel(server);
                jTabbedPane1.setSelectedComponent(serverPanel);
                server_nameField.grabFocus();
            }
            if (classname.equals("Credential")) {
                Credential credential = (Credential)node.getUserObject();
                clear_panels();
//                PasswordStoreSearch pwss = new PasswordStoreSearch(pws, credential);
//                populate_tree(pwss);
                populate_credential_panel(credential);
                jTabbedPane1.setSelectedComponent(credPanel);
                cred_descField.grabFocus();
            }
        }
    }//GEN-LAST:event_browseTreeValueChanged

    private void expandTree() {
        int row = 0;
        while (row < browseTree.getRowCount()) {
            browseTree.expandRow(row);
            row++;
        } 
    }
    
    private void open_default_store(){
        String root_path = get_path();
        String data_dir = root_path + "/data";
        String default_file_path = data_dir + "/password_store.xml";
        File default_data_dir = new File(data_dir);
        File default_file = new File(default_file_path);
        String [] data_options = {default_file_path,"xml", PassVaultApp.getApplication().password};
        System.out.println(data_options[2]);
        if (! default_data_dir.exists()) {
            default_data_dir.mkdir();
        }
        if (! default_file.exists()) {
            PassVaultApp.getApplication().store_file = data_options;
            save_data();
        }
        PassVaultApp.getApplication().store_file = data_options;
        open_store(data_options);
        populate_tree();
    }
    
    private void open_store(String [] data_options) {
            StoreReader store_reader = new StoreReader(data_options);
            PasswordStore new_pws = store_reader.read_store_data();
            PassVaultApp.getApplication().load_password_data(new_pws);   
    }
    
    private void openItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openItemActionPerformed
        // TODO add your handling code here:
        FileFilter filter =  new ExtensionFileFilter("XML", new String[] { "XML"});
        fileChooser.setFileFilter(filter);
        fileChooser.showOpenDialog(null);
        if (fileChooser.getSelectedFile() !=null ) {
            String datafile = fileChooser.getSelectedFile().getAbsolutePath();
            System.out.println("Opening => " + datafile);
            String [] store_file = PassVaultApp.getApplication().store_file;
            store_file[0] = datafile;
            store_file[1] = "xml";
            String [] data_options = {datafile, "xml",PassVaultApp.getApplication().password};
            StoreReader store_reader = new StoreReader(data_options);
            PasswordStore new_pws = store_reader.read_store_data();
            PassVaultApp.getApplication().load_password_data(new_pws);
            populate_tree();
        }
}//GEN-LAST:event_openItemActionPerformed

    private void fileChooserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileChooserActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_fileChooserActionPerformed

    private boolean save_data(){
        boolean result = false;
        String [] file_info =  PassVaultApp.getApplication().store_file;
        if (file_info[0] != null) {
            file_info[2] = PassVaultApp.getApplication().password;
            PasswordStore pws = PassVaultApp.getApplication().passwordStore;
            StoreWriter store_writer = new StoreWriter(file_info,pws);
            result = store_writer.write_store_data();
        }
        return result;
    }
    
    private void saveItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveItemActionPerformed
        // TODO add your handling code here:
        boolean result = save_data();
        if (result == true) {
            statusMessageLabel.setText("Data Written.");
        }
}//GEN-LAST:event_saveItemActionPerformed

    private void load_default_data(){
        String [] options = {"lib/passwordstore.xml","xml"};
        StoreReader sr = new StoreReader(options);
        PassVaultApp.getApplication().passwordStore = sr.read_store_data();
    }
    
    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        // TODO add your handling code here:
        save_data();
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void delete_credButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delete_credButtonActionPerformed
        // TODO add your handling code here:
        PassVaultApp.getApplication().passwordStore.remove_credential(cred_descField.getText());
        clear_panels();
        populate_tree();
    }//GEN-LAST:event_delete_credButtonActionPerformed

    private void delete_personButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delete_personButtonActionPerformed
        // TODO add your handling code here:
        PassVaultApp.getApplication().passwordStore.remove_person(person_nameField.getText());
        clear_panels();
        populate_tree();
    }//GEN-LAST:event_delete_personButtonActionPerformed

    private void delete_serverButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delete_serverButtonActionPerformed
        // TODO add your handling code here:
        PassVaultApp.getApplication().passwordStore.remove_server(server_nameField.getText());
        clear_panels();
        populate_tree();
    }//GEN-LAST:event_delete_serverButtonActionPerformed

    
    
    public String get_path(){
        String jarpath = get_jar_path();
        Pattern pattern = Pattern.compile("file:(/.+/)PassVault.jar");
        Matcher matcher = pattern.matcher(jarpath);
        if (! matcher.find()) {
            pattern = Pattern.compile("file:(/.+/)PasswordStore.class");
            matcher = pattern.matcher(jarpath);
            boolean matched = matcher.find();
        }
        String the_path = matcher.group(1);
        return the_path;
    }
    
    public String get_jar_path() {
        Class c = PasswordStore.class;
        String thepath = c.getResource(c.getSimpleName() + ".class").toString();
        Pattern path_match = Pattern.compile("file:/(.+)/PasswordStore.class");
        
        return thepath;
    }
    
    private int password_strength(String password){
        PasswordCheck pwc = new PasswordCheck();
        int strength = pwc.CheckPasswordStrength(password);
        return strength;
    }
    
    private void update_password_strength_info(){
       int strength = password_strength(cred_passwordField.getText());
        String verdict;
        if (strength < 16) {
			verdict = "very weak";
		} else if (strength > 15 && strength < 25) {
			verdict = "weak";
		} else if (strength > 24 && strength < 35) {
			verdict = "mediocre";
		} else if (strength > 34 && strength < 45) {
			verdict = "strong";
		} else {
			verdict = "very strong";
		}
        strengthBar.setValue(strength);
        passwordVerdict.setText(verdict); 
    }
    
    private void cred_passwordFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cred_passwordFieldKeyTyped
        // TODO add your handling code here:
//        progressBar.setValue(password_strength(cred_passwordField.getText()));
        update_password_strength_info();
    }//GEN-LAST:event_cred_passwordFieldKeyTyped

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTree browseTree;
    private javax.swing.JButton clearentryButton;
    private javax.swing.JButton clearpersonButton;
    private javax.swing.JButton clearserverButton;
    private javax.swing.JPanel credPanel;
    private javax.swing.JTextField cred_addressField;
    private javax.swing.JTextField cred_descField;
    private javax.swing.JButton cred_genpassButton;
    private javax.swing.JTextField cred_loginField;
    private javax.swing.JTextArea cred_notesField;
    private javax.swing.JTextField cred_passwordField;
    private javax.swing.JComboBox cred_personCombo;
    private javax.swing.JComboBox cred_serverCombo;
    private javax.swing.JButton delete_credButton;
    private javax.swing.JButton delete_personButton;
    private javax.swing.JButton delete_serverButton;
    private javax.swing.JFileChooser fileChooser;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JDialog openDialog;
    private javax.swing.JLabel passwordVerdict;
    private javax.swing.JPanel personPanel;
    private javax.swing.JTextField person_emailField;
    private javax.swing.JTextField person_nameField;
    private javax.swing.JTextArea person_notesField;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JButton refreshTreeButton;
    private javax.swing.JMenuItem saveItem;
    private javax.swing.JButton saveentryButton;
    private javax.swing.JButton savepersonButton;
    private javax.swing.JButton saveserverButton;
    private javax.swing.JPanel serverPanel;
    private javax.swing.JTextField server_addressField;
    private javax.swing.JTextField server_nameField;
    private javax.swing.JTextArea server_notesField;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JProgressBar strengthBar;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;
}

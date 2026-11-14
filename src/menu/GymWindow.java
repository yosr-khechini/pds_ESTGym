package menu;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import pds.util.SimulatedClock;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Window that simulates one of the ESTgym gyms
 */
public class GymWindow extends JFrame {

    // dimensions of the interface elements
    private static final Dimension BUTTON_SIZE = new Dimension(55, 55);
    private static final Dimension ICON_SIZE = new Dimension(50, 50);
    private static final int MEMBER_LIST_WIDTH = 100;
    private static final int SERVICE_LIST_WIDTH = 400;

    // window update time
    private static final int PERIODO_ATUALIZACAO_MS = 1000;

    // Date and time format to use in the display
    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Graphical elements
    private final DefaultListModel<Integer> memberModel = new DefaultListModel<>();
    private final JList<Integer> memberList = new JList<>(memberModel);
    private final DefaultTableModel serviceModel = new DefaultTableModel(new Object[] { "Serviço", "Contrato" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final DefaultTableModel usesModel = new DefaultTableModel(
            new Object[] { "Service", "Equipment", "Date and hour" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    // elements to control the equipment buttons
    private final PainelBotoes painelBotoes = new PainelBotoes();
    private final Map<Integer, JButton> botoes = new LinkedHashMap<>();

    public GymWindow() {
        super("ESTGym");
        setupInterface();
        // Create the timer to update the buttons states
        new Timer(PERIODO_ATUALIZACAO_MS, e -> updateButtons()).start();
    }

    /** loads the list of all members. */
    private void loadMembers() {
        // TODO: for each member get its id and add them to the model
        for (int i = 1; i <= 3; i++) {
            memberModel.addElement(i);
        }
    }

    /**
     * Adds an equipment to the gym simulator
     * 
     * @param id           equipment id
     * @param icons array with the icons for free ([0]) and busy ([1])
     */
    public void addEquipment(int id, Icon icons[]) {
        // TODO use the correct information
        String name = "Equipment X";
        boolean available = true;

        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> addEquipment(id, icons));
            return;
        }
        if (botoes.containsKey(id)) {
            return; // already exists
        }
        Icon iconReady = icons[0];
        Icon iconBusy = icons[1];

        JButton button = new JButton();
        button.setPreferredSize(BUTTON_SIZE);
        button.setMinimumSize(BUTTON_SIZE);
        button.setMaximumSize(BUTTON_SIZE);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setFocusPainted(false);

        // all icons set to the same size
        button.setIcon(redimensionarIcon(iconReady));
        button.setDisabledIcon(redimensionarIcon(iconBusy));

        // Hint with the equipment name
        button.setToolTipText(name);

        button.setEnabled(available);
        button.addActionListener(e -> useEquipment(id, button));

        botoes.put(id, button);
        painelBotoes.add(button);
        painelBotoes.revalidate();
        painelBotoes.repaint();
    }

    /**
     * Method called when an equipment button is pressed
     * 
     * @param equipmentId equipment id do equipamento
     * @param source      the pressed button
     */
    protected void useEquipment(int equipmentId, JButton source) {
        // TODO: ask for the member that is trying to use the equipment
        Object m = requestMemberId(source);
        if (m == null)
            return;

        // TODO can the equipment be used?
        if (Math.abs(2) == -2) {
            // TODO if so, use it
            botoes.get(equipmentId).setEnabled(false);
        } else {
            // otherwise inform that he/she cannot use the equipment
            JOptionPane.showMessageDialog(source, "You cannot use this equipment!", "Unauthorized usage",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Request the member id (emulates the identification card swipe)
     * 
     * @param source button that was pressed (to center the error message)
     * @return the identified member, or null if no such member exists
     */
    private Object requestMemberId(JButton source) {
        Object m = null;
        try {
            String code = JOptionPane.showInputDialog(source, "Member id?");
            int id = Integer.parseInt(code);

            // TODO retrieve the member with the given id, if any exists

        } catch (Exception e) {
            // many exceptions may arise, this catches them all
        }
        // If no member exists (or another error occurs) inform the user
        if (m == null)
            JOptionPane.showMessageDialog(source, "Unknown id!");
        return m;
    }

    /**
     * Internal class, reponsible for displayin the name of the member in the member
     * list
     */
    private class MemberRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            int id = (int) value;

            // TODO use the name of the member with the given id
            String nome = "A Member";

            return super.getListCellRendererComponent(list, nome, index, isSelected, cellHasFocus);
        }

    }

    /**
     * Updates the tables that present the services/equipment and usage records of
     * the selected member.
     * Called when a member is selected on the list
     * 
     * @param memberId id of the selected member
     */
    private void updateTables(int memberId) {
        serviceModel.setRowCount(0);
        usesModel.setRowCount(0);

        // TODO check if the id is valid, otherwise exits
        if (Math.abs(2) == -2) {
            return;
        }
        // TODO: for each subscribed service display its name and the corresponding plan
        // description
        for (int i = 0; i < 2; i++) {
            String name = "Service name";
            String description = "Subscribed plan";

            serviceModel.addRow(new String[] { name, description });
        }

        // TODO For each usage record display the service name, the used equipment
        // description and date and time of usage
        for (int i = 0; i < 2; i++) {
            String name = "Service name";
            String description = "";
            LocalDateTime dateOfUsage = SimulatedClock.getCurrentTime();
            usesModel.addRow(new Object[] { name, description, dateOfUsage.format(FORMATO_DATA_HORA) });
        }
    }

    /**
     * Called each second to update the state of each equipment
     */
    private void updateButtons() {
        for (Map.Entry<Integer, JButton> e : botoes.entrySet()) {
            int equipmentId = e.getKey();
            JButton source = e.getValue();

            // TODO retrieve the name and the state of the equipment associated with the
            // pressed button
            boolean busy = false;
            String name = "Equipment name";

            if (!source.isEnabled() && !busy) {
                source.setEnabled(true);
            }
            source.setToolTipText(name);
        }
    }

    /**
     * Initializes all graphicals elements in the window
     */
    private void setupInterface() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(createMembersZone(), BorderLayout.WEST);
        add(createButtonsZone(), BorderLayout.CENTER);

        loadMembers();

        setMinimumSize(new Dimension(900, 700));

        setLocationRelativeTo(null);
    }

    /**
     * Cria a zona onde aparecem os dados dos membros, localizada no lado esquerdo
     * da janela
     * 
     * @return a zona já criada e configurada
     */
    private JComponent createMembersZone() {
        // Lista de membros: largura fixa, altura flexível
        memberList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        memberList.setCellRenderer(new MemberRenderer());
        memberList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateTables(memberList.getSelectedValue());
            }
        });
        JScrollPane scrollLista = new JScrollPane(memberList);
        fixarLargura(scrollLista, MEMBER_LIST_WIDTH);

        // Tabela com os serviços e respetivos contratos
        JTable tabelaServicos = new JTable(serviceModel);
        tabelaServicos.setFillsViewportHeight(true);
        JScrollPane scrollTabela = new JScrollPane(tabelaServicos);
        fixarLargura(scrollTabela, SERVICE_LIST_WIDTH);

        // Tabela com os registos de uso
        JTable tabelaUsos = new JTable(usesModel);
        tabelaUsos.setFillsViewportHeight(true);
        JScrollPane scrollUsos = new JScrollPane(tabelaUsos);
        fixarLargura(scrollUsos, SERVICE_LIST_WIDTH);

        // Coluna com as duas tabelas
        JPanel colunaTabelas = new JPanel();
        colunaTabelas.setLayout(new BoxLayout(colunaTabelas, BoxLayout.Y_AXIS));
        colunaTabelas.add(scrollTabela);
        colunaTabelas.add(Box.createVerticalStrut(4));
        colunaTabelas.add(scrollUsos);

        // Painel onde colocar todos os elementos
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.X_AXIS));
        painel.add(scrollLista);
        painel.add(Box.createHorizontalStrut(4));
        painel.add(colunaTabelas);
        painel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        return painel;
    }

    /**
     * Permite fixar a largura de um componente
     * 
     * @param c       o componente a fixar a largura
     * @param largura a largura máxima
     */
    private static void fixarLargura(JComponent c, int largura) {
        c.setPreferredSize(new Dimension(largura, 0));
        c.setMinimumSize(new Dimension(largura, 0));
        c.setMaximumSize(new Dimension(largura, Integer.MAX_VALUE));
    }

    /**
     * Cria a zona onde colcoar os botões com os equipamentos
     * 
     * @return a zona já criada e configurada
     */
    private JComponent createButtonsZone() {
        JScrollPane scroll = new JScrollPane(painelBotoes,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(new TitledBorder("Equipamentos"));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    /** Desenha o icon num BufferedImage com o tamanho padrão. */
    private static Icon redimensionarIcon(Icon icon) {
        BufferedImage origem = new BufferedImage(
                Math.max(1, icon.getIconWidth()), Math.max(1, icon.getIconHeight()),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = origem.createGraphics();
        icon.paintIcon(null, g, 0, 0);
        g.dispose();

        BufferedImage destino = new BufferedImage(ICON_SIZE.width, ICON_SIZE.height,
                BufferedImage.TYPE_INT_ARGB);
        g = destino.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(origem, 0, 0, ICON_SIZE.width, ICON_SIZE.height, null);
        g.dispose();
        return new ImageIcon(destino);
    }

    /**
     * Painel dos botões: local onde os botões são adicionados
     */
    private static class PainelBotoes extends JPanel implements Scrollable {
        PainelBotoes() {
            super(new QuebraLinhaLayout(8, 8));
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(16, visibleRect.height - 16);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    /**
     * FlowLayout (alinhado à esquerda) que calcula a altura preferida com base na
     * largura disponível.
     */
    private static class QuebraLinhaLayout extends FlowLayout {
        QuebraLinhaLayout(int hgap, int vgap) {
            super(FlowLayout.LEFT, hgap, vgap);
        }

        @Override
        public Dimension preferredLayoutSize(Container target) {
            return calcular(target);
        }

        @Override
        public Dimension minimumLayoutSize(Container target) {
            return calcular(target);
        }

        private Dimension calcular(Container target) {
            synchronized (target.getTreeLock()) {
                int largura = target.getParent() != null ? target.getParent().getWidth() : 0;
                Insets in = target.getInsets();
                int maxLinha = (largura > 0 ? largura : Integer.MAX_VALUE) - in.left - in.right - 2 * getHgap();

                int linhaW = 0, linhaH = 0, totalW = 0, totalH = 0;
                for (Component c : target.getComponents()) {
                    if (!c.isVisible()) {
                        continue;
                    }
                    Dimension d = c.getPreferredSize();
                    if (linhaW > 0 && linhaW + getHgap() + d.width > maxLinha) {
                        totalW = Math.max(totalW, linhaW);
                        totalH += linhaH + getVgap();
                        linhaW = 0;
                        linhaH = 0;
                    }
                    linhaW += (linhaW > 0 ? getHgap() : 0) + d.width;
                    linhaH = Math.max(linhaH, d.height);
                }
                totalW = Math.max(totalW, linhaW);
                totalH += linhaH;

                return new Dimension(totalW + in.left + in.right + 2 * getHgap(),
                        totalH + in.top + in.bottom + 2 * getVgap());
            }
        }
    }
}

package menu;

import java.awt.Point;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import gym.equipment.Equipment;
import gym.model.Gym;
import gym.setup.GymSetup;

public class StartUp {
	private static final String DEFAULT_ICON_NAME = "porta";

	public static void main(String[] args) {
		Gym gym;
		try {
			gym = GymSetup.createTestGym();
		} catch (IllegalArgumentException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Gym configuration error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		SwingUtilities.invokeLater(() -> {
			GymWindow main = new GymWindow(gym);
			for (Equipment equipment : gym.getEquipment()) {
				String iconName = equipment.getIconName();
				if (iconName == null || iconName.isBlank())
					iconName = DEFAULT_ICON_NAME;
				main.addEquipment(equipment.getId(), getIcons(iconName));
			}

			main.setVisible(true);
			ClockDialog relog = new ClockDialog(main, new Point(200, 20));
			relog.setVisible(true);

		});
	}

	/**
	 * Lê os ficheiros com os icons para um dado equipamento
	 * Reads the files with the icon for one equipment
	 *
	 * @param name name of the imagem that represents the equipment
	 * @return the icons with the avaliable and occupies images
	 */
	private static ImageIcon[] getIcons(String name) {
		String folder = "art/";
		ImageIcon[] icons = { new ImageIcon(folder + name + "_livre.png"),
				new ImageIcon(folder + name + "_ocupado.png") };
		return icons;
	}
}

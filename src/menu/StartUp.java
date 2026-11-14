package menu;

import java.awt.Point;
import java.time.LocalDateTime;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import pds.util.SimulatedClock;

public class StartUp {

	public static void main(String[] args) {
		// Creates the simulated clock and sets it to the current time
		SimulatedClock relogio = SimulatedClock.getSimulatedClock();
		relogio.setTempoAtual(LocalDateTime.now());

		// TODO create the services

		// TODO create the equipments

		// TODO create the members

		// TODO create plan for each member

		SwingUtilities.invokeLater(() -> {
			GymWindow main = new GymWindow();

			// TODO add the equipment to the simulator (the given ids are just examples)
			// note: the names of the image files are not translated
			main.addEquipment(10, getIcons("porta")); // door
			main.addEquipment(11, getIcons("bebida")); // drink
			main.addEquipment(12, getIcons("duche")); // shower
			main.addEquipment(13, getIcons("pt")); // personal trainer
			main.addEquipment(14, getIcons("nutricionista")); // nutricionist
			main.addEquipment(15, getIcons("poltrona")); // massage chair
			main.addEquipment(16, getIcons("aula")); // class
			main.addEquipment(17, getIcons("fisica")); // physical evaluation
			main.addEquipment(18, getIcons("porta_piscina")); // pool door

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
		ImageIcon icons[] = { new ImageIcon(folder + name + "_livre.png"),
				new ImageIcon(folder + name + "_ocupado.png") };
		return icons;
	}
}

package application;

import java.util.Scanner;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import view.ConsoleView;

/**
 * Główny punkt wejścia dla Aplikacji Kalendarza. Klasa rozszerza
 * {@code Application} z JavaFX, aby zapewnić opcje zarówno dla konsoli, jak i
 * dla interfejsu użytkownika (GUI) do uruchamiania aplikacji. Menu konsolowe
 * pozwala użytkownikowi wybrać między uruchomieniem aplikacji konsolowej a
 * aplikacją GUI. Aplikacja konsolowa jest inicjowana poprzez utworzenie
 * instancji {@link ConsoleView} i wywołanie jej metody {@code init()}.
 * Aplikacja GUI jest uruchamiana poprzez wywołanie metody {@code launch(args)}.
 * 
 * @see Application
 * @see ConsoleView
 */
public class Program extends Application
{
	/**
	 * Metoda główna do uruchamiania programu. Umożliwia użytkownikowi wybór między
	 * uruchomieniem aplikacji konsolowej a GUI.
	 *
	 * @param args tablica argumentów przekazywanych do programu z wiersza poleceń
	 *             podczas jego uruchamiania
	 */
	public static void main(String[] args)
	{
		Scanner scanner = new Scanner(System.in);
		boolean exit = false;

		while (!exit)
		{
			System.out.println("Console running, choose one option:");
			System.out.println("1. Run console application.");
			System.out.println("2. Run GUI application.");
			System.out.println("3. Exit.");
			System.out.println();
			System.out.print("Option: ");

			int choice = scanner.nextInt();

			switch (choice)
			{
				case 1:
				{
					new ConsoleView().init();
					exit = true;
					break;
				}
				case 2:
				{
					launch(args);
					exit = true;
					break;
				}
				case 3:
				{
					System.out.println("Exiting program.");
					exit = true;
					break;
				}
				default:
					System.out.println("Incorrect choice. Try again.");
			}
		}

		System.out.println();
		scanner.close();
	}

	/**
	 * Metoda start służąca do uruchamiania aplikacji GUI. Wczytuje główny widok z
	 * pliku FXML.
	 *
	 * @param  stage     Główny widok dla aplikacji.
	 * @throws Exception Jeśli wystąpi błąd podczas uruchamiania aplikacji GUI.
	 */
	@Override
	public void start(Stage stage) throws Exception
	{
		try
		{
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/MainView.fxml"));
			Scene scene = new Scene(fxmlLoader.load());
			stage.setTitle("Calendar Application");
			stage.setScene(scene);
			stage.show();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}

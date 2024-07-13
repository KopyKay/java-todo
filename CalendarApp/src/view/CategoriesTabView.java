package view;

import java.util.List;

import application.Controller;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.Category;

/**
 * Klasa obsługująca widok zakładki z kategoriami w aplikacji. Umożliwia
 * dodawanie, przeglądanie, aktualizowanie oraz usuwanie kategorii. Implementuje
 * graficzny interfejs użytkownika przy użyciu JavaFX.
 */
public class CategoriesTabView
{
	private Controller controller;
	private CalendarView calendarView;
	private Alert alert;

	private List<Category> categoryList;

	@FXML
	private ColorPicker colorPickerCategory_Color;

	@FXML
	private ListView<Category> listViewCategory_CategoryList;

	@FXML
	private TextField textFieldCategory_Name;

	/**
	 * Obsługuję zdarzenie kliknięcia przycisku "Add New Category". Dodaje nową
	 * kategorię na podstawie wprowadzonych danych i odświeża listę kategorii.
	 */
	@FXML
	private void buttonCategory_AddNewCategory_Click(ActionEvent actionEvent)
	{
		String categoryName = textFieldCategory_Name.getText();
		Color categoryColor = colorPickerCategory_Color.getValue();

		if (categoryName.isBlank() || categoryColor == null)
		{
			alert.setAlertType(AlertType.WARNING);
			alert.setHeaderText("Empty fields");
			alert.setContentText("Fields cannot be empty!");
			alert.showAndWait();
			return;
		}

		if (this.controller.isCategoryExists(categoryName))
		{
			alert.setAlertType(AlertType.WARNING);
			alert.setHeaderText("Category already exists");
			alert.setContentText("An category with the same name [" + categoryName + "] already exists!");
			alert.showAndWait();
			return;
		}

		String categoryColorHex = this.controller.convertColorToHex(categoryColor);

		try
		{
			this.controller.addNewCategory(categoryName, categoryColorHex);
			clearFields();
			refreshCategoryList();
			this.calendarView.refreshCalendar();
		}
		catch (Exception ex)
		{
			alert.setAlertType(AlertType.ERROR);
			alert.setHeaderText("Something goes wrong");
			alert.setContentText(ex.getMessage());
			alert.showAndWait();
		}
	}

	/**
	 * Obsługuję zdarzenie kliknięcia przycisku "Clear Fields". Czyści wprowadzone
	 * dane w polach formularza.
	 */
	@FXML
	private void buttonCategory_ClearFields_Click(ActionEvent actionEvent)
	{
		clearFields();
	}

	/**
	 * Obsługuje zdarzenie kliknięcia przycisku "Delete". Usuwa zaznaczoną kategorię
	 * z listy. Wyświetla ostrzeżenie, jeśli nie wybrano żadnej kategorii.
	 *
	 * @param actionEvent Zdarzenie akcji przycisku.
	 */
	@FXML
	private void buttonCategory_Delete_Click(ActionEvent actionEvent)
	{
		Category c = listViewCategory_CategoryList.getSelectionModel().getSelectedItem();

		if (c == null)
		{
			alert.setAlertType(AlertType.WARNING);
			alert.setHeaderText("No selected category");
			alert.setContentText("Select category from list to delete it!");
			alert.showAndWait();
			return;
		}

		try
		{
			this.controller.deleteCategory(c);
			refreshCategoryList();
			this.calendarView.refreshCalendar();
		}
		catch (Exception ex)
		{
			alert.setAlertType(AlertType.WARNING);
			alert.setHeaderText("Cannot delete this category [" + c + "]");
			alert.setContentText(ex.getMessage());
			alert.showAndWait();
			ex.printStackTrace();
		}
	}

	/**
	 * Obsługuje zdarzenie kliknięcia przycisku "Update". Otwiera okno aktualizacji
	 * wybranej kategorii, jeżeli jakaś kategoria jest zaznaczone na liście,
	 * przekazując dalej referencję głównego kontrolera oraz danych wybranego
	 * obiektu kategorii. Dodatkowo dodaje ustawienie obsługi zdarzenia podczas
	 * zamknięcia okna, aby odświeżyło listę kategorii oraz kalendarz.
	 *
	 * @param actionEvent Zdarzenie akcji przycisku.
	 */
	@FXML
	private void buttonCategory_Update_Click(ActionEvent actionEvent)
	{
		if (listViewCategory_CategoryList.getSelectionModel().getSelectedItem() == null)
		{
			alert.setAlertType(AlertType.WARNING);
			alert.setHeaderText("No selected category");
			alert.setContentText("Select category from list to update it!");
			alert.showAndWait();
			return;
		}

		try
		{
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("UpdateCategoryPopupView.fxml"));
			Parent root = fxmlLoader.load();

			UpdateCategoryPopupView updateCategoryPopup = fxmlLoader.getController();
			Category selectedCategory = listViewCategory_CategoryList.getSelectionModel().getSelectedItem();
			updateCategoryPopup.init(this.controller, selectedCategory);

			Stage stage = new Stage();
			stage.initStyle(StageStyle.UTILITY);
			stage.setScene(new Scene(root));

			stage.setOnHidden(e ->
			{
				refreshCategoryList();
				this.calendarView.refreshCalendar();
			});

			stage.show();
		}
		catch (Exception ex)
		{
			alert.setAlertType(AlertType.WARNING);
			alert.setHeaderText("Something goes wrong");
			alert.setContentText("Can't load update window.\n" + ex.getMessage());
			alert.showAndWait();
			ex.printStackTrace();
		}
	}

	@FXML
	private void colorPickerCategory_Color_Select(ActionEvent event)
	{

	}

	/**
	 * Czyści pola formularza dodawania nowej kategorii.
	 */
	private void clearFields()
	{
		textFieldCategory_Name.clear();
		colorPickerCategory_Color.setValue(Color.valueOf("#ffffff"));
	}

	/**
	 * Odświeża listę kategorii w interfejsie użytkownika, używając domyślnego
	 * sortowania.
	 */
	public void refreshCategoryList()
	{
		listViewCategory_CategoryList.getItems().clear();

		this.controller.sortCategoryByDefault();

		listViewCategory_CategoryList.getItems().addAll(this.categoryList);
	}

	/**
	 * Inicjalizuje widok zakładki z kategoriami, ustawiając referencje do
	 * kontrolera, widoku kalendarza oraz wczytując dane o kategoriach. Dodatkowo,
	 * inicjalizuje interfejs użytkownika, ustawiając domyślne wartości i
	 * ustawienia.
	 *
	 * @param controller   Obiekt kontrolera, który zarządza logiką biznesową.
	 * @param calendarView Widok kalendarza, z którym ta klasa współpracuje.
	 */
	public void init(Controller controller, CalendarView calendarView)
	{
		this.controller = controller;
		this.calendarView = calendarView;

		this.categoryList = this.controller.getCategories();
		this.alert = new Alert(AlertType.NONE);

		refreshCategoryList();
		colorPickerCategory_Color.setValue(Color.valueOf("#ffffff"));
	}
}

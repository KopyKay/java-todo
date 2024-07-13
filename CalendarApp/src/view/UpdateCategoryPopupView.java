package view;

import application.Controller;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.Category;

/**
 * Ta klasa reprezentuje widok do aktualizacji kategorii w wyskakującym oknie.
 */
public class UpdateCategoryPopupView
{
	private Controller controller;
	private Alert alert;

	private Category category;

	@FXML
	private ColorPicker colorPickerCategory_Color;

	@FXML
	private TextField textFieldCategory_Name;

	/**
	 * Obsługuje zdarzenie anulowania aktualizacji kategorii.
	 *
	 * @param actionEvent Zdarzenie akcji.
	 */
	@FXML
	private void buttonCategory_Cancel_Click(ActionEvent actionEvent)
	{
		closeWindow(actionEvent);
	}

	/**
	 * Obsługuje zdarzenie przycisku aktualizacji kategorii w widoku edycji. Pobiera
	 * dane z pól formularza, waliduje je, a następnie wywołuje metodę kontrolera do
	 * aktualizacji kategorii. W przypadku błędów wyświetla odpowiednie ostrzeżenia.
	 *
	 * @param actionEvent Zdarzenie akcji przycisku.
	 */
	@FXML
	private void buttonCategory_UpdateSelectedCategory_Click(ActionEvent actionEvent)
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

		int categoryId = this.category.getId();

		if (this.controller.isCategoryExists(categoryId, categoryName))
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
			this.controller.updateCategory(this.category, categoryName, categoryColorHex);
			closeWindow(actionEvent);
		}
		catch (Exception ex)
		{
			alert.setAlertType(AlertType.ERROR);
			alert.setHeaderText("Something goes wrong");
			alert.setContentText(ex.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	private void colorPickerCategory_Color_Select(ActionEvent actionEvent)
	{

	}

	/**
	 * Zamyka aktualne okno, w którym kategoria jest aktualizowana.
	 *
	 * @param actionEvent Zdarzenie akcji, które wywołało zamknięcie okna.
	 */
	private void closeWindow(ActionEvent actionEvent)
	{
		Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
		stage.close();
	}

	/**
	 * Ustawia dane wybranej kategorii w formularzu aktualizacyjnym, wcześniej
	 * wybranego z listy w {@code view.CategoriesTabView}.
	 */
	private void setCategoryData()
	{
		textFieldCategory_Name.setText(this.category.getName());
		colorPickerCategory_Color.setValue(Color.valueOf(this.category.getColorHex()));
	}

	/**
	 * Inicjalizuje widok wyskakującego okna aktualizacji kategorii ustawiając
	 * domyślne wartości i ustawienia oraz referencję do kontrolera i wczytuje dane
	 * o wydarzeniach.
	 *
	 * @param controller Kontroler aplikacji.
	 * @param category   Aktualizowana kategoria.
	 */
	public void init(Controller controller, Category category)
	{
		this.controller = controller;
		this.category = category;
		this.alert = new Alert(AlertType.NONE);

		setCategoryData();
	}
}

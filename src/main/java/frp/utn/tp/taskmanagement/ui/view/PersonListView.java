package frp.utn.tp.taskmanagement.ui.view;

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.theme.lumo.LumoUtility;

import frp.utn.tp.base.ui.component.ViewToolbar;
import frp.utn.tp.taskmanagement.domain.Person;
import frp.utn.tp.taskmanagement.service.PersonService;
import jakarta.annotation.security.PermitAll;

import java.util.Collections;

@Route("person-list")
@PageTitle("Lista de Personas")
@Menu(order = 0, icon = "vaadin:users", title = "Personas")
@PermitAll
public class PersonListView extends Main {

    private Binder<Person> binder = new BeanValidationBinder<>(Person.class);
    private TextField lastName = new TextField("Apellido");
    private TextField firstName = new TextField("Nombre");
    private TextField dni = new TextField("DNI");
    
    private Button newPersonButton = new Button("Nuevo"); 
    private Button saveButton = new Button("Guardar");
    
    private Grid<Person> personGrid;
    private PersonService personService;

    private HorizontalLayout formPersona;

    private HorizontalLayout buttonsLayout; 

    public PersonListView(PersonService personService) {
        this.personService = personService;

        binder.bindInstanceFields(this);

        add(new ViewToolbar("Administrador de Personas"));

        VerticalLayout content = new VerticalLayout();

        // Inicializamos los layouts
        formPersona = new HorizontalLayout();
        formPersona.setSpacing(true);
        formPersona.getStyle().set("gap", "5px");
        
        // Asignamos los campos al layout del formulario
        formPersona.add(firstName, lastName, dni);

        buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.getStyle().set("gap", "5px");

        // Configuración inicial de los campos y botón "Guardar"
        firstName.setReadOnly(true);
        lastName.setReadOnly(true);
        dni.setReadOnly(true);
        saveButton.setEnabled(false);
        
        // Ocultamos los campos del formulario y el botón de guardar por defecto
        formPersona.setVisible(false);
        saveButton.setVisible(false);

        // Configuración del botón "Nuevo"
        newPersonButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY); 
        newPersonButton.addClickListener(e -> {
            habilitarCampos(); 
            formPersona.setVisible(true); 
            saveButton.setVisible(true);
            saveButton.setEnabled(true);
            firstName.focus(); //enfoca el primer campo para facilitar la entrada
        });

        // Configuración del botón "Guardar"
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> createPerson());

        // Agregamos los botones al layout de botones. El orden importa.
        buttonsLayout.add(newPersonButton, saveButton);

        personGrid = new Grid<>();
        personGrid.setItems(query -> personService.list(toSpringPageRequest(query)).stream());
        personGrid.addColumn(Person::getId).setHeader("ID");
        personGrid.addColumn(Person::getNombre).setHeader("Nombre");
        personGrid.addColumn(Person::getApellido).setHeader("Apellido");
        personGrid.addColumn(Person::getDni).setHeader("DNI");
        
        personGrid.addComponentColumn(person -> {
            HorizontalLayout actions = new HorizontalLayout();

            Button viewTasksButton = new Button("Ver Tareas", new Icon(VaadinIcon.LIST), event -> {
                UI.getCurrent().navigate(TaskListView.class,
                    new QueryParameters(Collections.singletonMap("personId",
                                       Collections.singletonList(person.getId().toString()))));
            });
            viewTasksButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            viewTasksButton.setTooltipText("Ver Tareas de " + person.getNombre());

            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH), event -> {
                Dialog confirmDialog = new Dialog();
                confirmDialog.setHeaderTitle(person.toString());
                confirmDialog.add("¿Estas seguro que deseas eliminar esta persona?");

                Button confirmDeleteButton = new Button("Borrar", (e) -> {
                    deletePerson(person.getId());
                    confirmDialog.close();
                });
                confirmDeleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
                confirmDeleteButton.getStyle().set("margin-right", "auto");
                confirmDialog.getFooter().add(confirmDeleteButton);

                Button cancelButton = new Button("Cancelar", (e) -> confirmDialog.close());
                cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                confirmDialog.getFooter().add(cancelButton);

                confirmDialog.open();
            });
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR);
            deleteButton.setAriaLabel("Borrar");
            deleteButton.setTooltipText("Eliminar");

            actions.add(viewTasksButton, deleteButton);
            return actions;
        }).setHeader("Acciones");

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);

        
        content.add(formPersona, buttonsLayout, personGrid); 
        add(content);
    }

    // Crear una Persona
    private void createPerson() {
        personService.createPerson(firstName.getValue(), lastName.getValue(), dni.getValue());
        personGrid.getDataProvider().refreshAll();
        firstName.clear();
        lastName.clear();
        dni.clear();
        Notification.show("Persona Agregada", 3000, Notification.Position.BOTTOM_START)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        
        // Después de guardar, ocultamos los campos y el botón "Guardar" de nuevo
        // y reseteamos el estado para el próximo "Nuevo"
        firstName.setReadOnly(true);
        lastName.setReadOnly(true);
        dni.setReadOnly(true);
        saveButton.setEnabled(false);
        formPersona.setVisible(false);
        saveButton.setVisible(false);
    }

    // Eliminar una Persona
    private void deletePerson(Long id) {
        personService.deletePerson(id);
        personGrid.getDataProvider().refreshAll();
        Notification.show("Persona Eliminada", 3000, Notification.Position.BOTTOM_START)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    // Habilitar los campos para Ingresar un nueva persona
    private void habilitarCampos() {
        lastName.setReadOnly(false);
        firstName.setReadOnly(false);
        dni.setReadOnly(false);
        
    }
}
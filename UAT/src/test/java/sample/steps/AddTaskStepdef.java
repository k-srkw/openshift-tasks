package sample.steps;

import static com.codeborne.selenide.Selenide.*;

import java.util.Map;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class AddTaskStepdef {

    @Given("画面を開いている")
    public void 画面を開いている() {
        open("/");
    }
    
}

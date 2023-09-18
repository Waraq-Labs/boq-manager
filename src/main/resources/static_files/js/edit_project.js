/*
This array holds new products added during this session of the page. The existing products are already added as options
to the select box template, we only need to store new ones added by the user while on this page.
 */
const productsAddedInThisSession = [];

window.addEventListener("load", function () {
    document.addEventListener("click", function (e) {
        if (!e.target.matches(".add-location-boq-line")) return;

        const btn = e.target;
        const templateTargetSelector = btn.dataset.templateSelector;
        const addTargetSelector = btn.dataset.addTargetSelector;

        const newBoqLineItem = document.querySelector(templateTargetSelector).content.cloneNode(true);
        for (const productOption of productsAddedInThisSession) {
            const boqProductOption = document.createElement("option");
            boqProductOption.setAttribute("value", productOption.id);
            boqProductOption.appendChild(document.createTextNode(productOption.name));

            newBoqLineItem.querySelector(".boq-product-selection").appendChild(
                boqProductOption
            );
        }
        document.querySelector(addTargetSelector).appendChild(
            newBoqLineItem
        );
    });

    document.addEventListener("click", function (e) {
        if (!e.target.matches(".remove-boq-line")) return;

        e.target.closest(".boq-line").remove();
    });

    function createNewProduct(e) {
        e.preventDefault();

        const cancelButton = document.getElementById("cancel-new-product");
        const saveButton = document.getElementById("save-new-product");

        cancelButton.disabled = true;
        saveButton.disabled = true;

        saveButton.getElementsByClassName("spinner-grow")[0].classList.remove("d-none");

        fetch(e.target.getAttribute("action"), {
            method: "POST",
            body: new FormData(e.target)
        }).then((resp) => {
            // TODO: Handle error here
            return resp.json()
        }).then((data) => {
            productsAddedInThisSession.push(data);

            const newOptionTemplate = document.createElement("option");
            newOptionTemplate.setAttribute("value", data.id);
            newOptionTemplate.appendChild(
                document.createTextNode(data.name)
            );

            for (const boqSelect of document.getElementsByClassName("boq-product-selection")) {
                boqSelect.appendChild(newOptionTemplate.cloneNode(true));
            }
            bootstrap.Modal.getInstance("#new-product-modal").hide();
        })
    }

    document.getElementById("new-product-form").addEventListener("submit", createNewProduct);

    // Reset the modal
    document.getElementById("new-product-modal").addEventListener("show.bs.modal", function () {
        document.getElementById("new-product-name").value = "";

        const cancelButton = document.getElementById("cancel-new-product");
        const saveButton = document.getElementById("save-new-product");
        cancelButton.disabled = false;
        saveButton.disabled = false;

        saveButton.getElementsByClassName("spinner-grow")[0].classList.add("d-none");
    });

    document.getElementById("project-edit-form").addEventListener("submit", (e) => {
        e.preventDefault();

        fetch(e.target.action, {
            method: "POST",
            body: new FormData(e.target)
        }).then((response) => {
            return response.json()
        }).then((data) => {
            if (data.error !== null) {
                console.log(data.error);
            } else {
                console.log("success");
            }
        })
    })
});
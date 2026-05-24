(function () {
    const drawer = document.querySelector(".cart-drawer");
    const overlay = document.querySelector(".cart-drawer-overlay");
    if (!drawer || !overlay) {
        return;
    }

    const itemsContainer = document.querySelector("[data-cart-items]");
    const emptyState = document.querySelector("[data-cart-empty]");
    const subtotalEl = document.querySelector("[data-cart-subtotal]");
    const countEls = document.querySelectorAll("[data-cart-count], .nav-cart-badge");
    const messageEl = document.querySelector("[data-cart-message]");

    const money = new Intl.NumberFormat("en-LK", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });

    function openDrawer() {
        document.body.classList.add("cart-drawer-open");
        drawer.setAttribute("aria-hidden", "false");
        loadCart();
    }

    function closeDrawer() {
        document.body.classList.remove("cart-drawer-open");
        drawer.setAttribute("aria-hidden", "true");
    }

    function showMessage(message, isError) {
        if (!messageEl) return;
        messageEl.textContent = message && message !== "success" ? message : "";
        messageEl.classList.toggle("is-error", Boolean(isError));
    }

    function updateCount(count) {
        countEls.forEach((el) => {
            el.textContent = count;
        });
    }

    function renderCart(data) {
        const items = data.items || [];
        itemsContainer.innerHTML = "";
        emptyState.style.display = items.length ? "none" : "flex";
        subtotalEl.textContent = money.format(data.subtotal || 0);
        updateCount(data.count || 0);

        items.forEach((item) => {
            const row = document.createElement("div");
            row.className = "cart-drawer-item";
            row.innerHTML = `
                <div class="cart-drawer-img">
                    ${item.toyImageUrl ? `<img src="${item.toyImageUrl}" alt="">` : `<span>Toy</span>`}
                </div>
                <div class="cart-drawer-info">
                    <strong>${escapeHtml(item.toyName)}</strong>
                    <span>Rs. ${money.format(item.unitPrice || 0)}</span>
                    <div class="cart-drawer-qty">
                        <button type="button" data-cart-dec="${item.cartItemId}">-</button>
                        <input type="number" min="1" value="${item.quantity}" data-cart-qty="${item.cartItemId}">
                        <button type="button" data-cart-inc="${item.cartItemId}">+</button>
                    </div>
                </div>
                <div class="cart-drawer-total">
                    <button type="button" data-cart-remove="${item.cartItemId}" aria-label="Remove item">X</button>
                    <strong>Rs. ${money.format(item.totalPrice || 0)}</strong>
                </div>
            `;
            itemsContainer.appendChild(row);
        });
    }

    function escapeHtml(value) {
        return String(value || "")
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    async function postForm(url, values) {
        const body = new URLSearchParams(values);
        const response = await fetch(url, {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body
        });
        return response.json();
    }

    async function loadCart() {
        try {
            const response = await fetch("/cart/api");
            const data = await response.json();
            renderCart(data);
            if (!data.success && data.message) showMessage(data.message, true);
        } catch (error) {
            showMessage("Could not load cart.", true);
        }
    }

    document.querySelectorAll("[data-cart-open]").forEach((link) => {
        link.addEventListener("click", (event) => {
            event.preventDefault();
            openDrawer();
        });
    });

    document.querySelectorAll("[data-cart-close]").forEach((button) => {
        button.addEventListener("click", closeDrawer);
    });

    document.addEventListener("keydown", (event) => {
        if (event.key === "Escape") closeDrawer();
    });

    document.querySelectorAll(".add-to-cart-form").forEach((form) => {
        form.addEventListener("submit", async (event) => {
            event.preventDefault();
            const formData = new FormData(form);
            const data = await postForm("/cart/api/add", {
                toyId: formData.get("toyId"),
                quantity: formData.get("quantity") || "1"
            });
            renderCart(data);
            showMessage(data.message, !data.success);
            openDrawer();
        });
    });

    let quantityTimer;
    async function updateDrawerQuantity(input) {
        const data = await postForm("/cart/api/update", {
            cartItemId: input.dataset.cartQty,
            quantity: input.value || "1"
        });
        renderCart(data);
        showMessage(data.message, !data.success);
    }

    itemsContainer.addEventListener("input", (event) => {
        const input = event.target.closest("[data-cart-qty]");
        if (!input) return;
        clearTimeout(quantityTimer);
        quantityTimer = setTimeout(() => updateDrawerQuantity(input), 350);
    });

    itemsContainer.addEventListener("change", async (event) => {
        const input = event.target.closest("[data-cart-qty]");
        if (!input) return;
        clearTimeout(quantityTimer);
        await updateDrawerQuantity(input);
    });

    itemsContainer.addEventListener("click", async (event) => {
        const remove = event.target.closest("[data-cart-remove]");
        const inc = event.target.closest("[data-cart-inc]");
        const dec = event.target.closest("[data-cart-dec]");

        if (remove) {
            const data = await postForm("/cart/api/remove", { cartItemId: remove.dataset.cartRemove });
            renderCart(data);
            showMessage(data.message, !data.success);
            return;
        }

        const button = inc || dec;
        if (!button) return;
        const cartItemId = inc ? inc.dataset.cartInc : dec.dataset.cartDec;
        const input = itemsContainer.querySelector(`[data-cart-qty="${cartItemId}"]`);
        const nextQuantity = Math.max(1, Number(input.value || 1) + (inc ? 1 : -1));
        const data = await postForm("/cart/api/update", {
            cartItemId,
            quantity: String(nextQuantity)
        });
        renderCart(data);
        showMessage(data.message, !data.success);
    });
})();

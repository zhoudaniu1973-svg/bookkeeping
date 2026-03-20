const STORAGE_KEY = "bookkeeping-web-data-v1";

const BILL_TYPE_LABELS = {
    EXPENSE: "支出",
    INCOME: "收入",
};

const FREQUENCY_LABELS = {
    DAILY: "每日",
    WEEKLY: "每周",
    MONTHLY: "每月",
    YEARLY: "每年",
};

const DEFAULT_CATEGORIES = [
    { name: "餐饮", icon: "🍜", color: "#FF6B6B", type: "EXPENSE", isDefault: true },
    { name: "交通", icon: "🚌", color: "#4A90D9", type: "EXPENSE", isDefault: true },
    { name: "购物", icon: "📦", color: "#9B59B6", type: "EXPENSE", isDefault: true },
    { name: "娱乐", icon: "🎮", color: "#E67E22", type: "EXPENSE", isDefault: true },
    { name: "居住", icon: "🏠", color: "#27AE60", type: "EXPENSE", isDefault: true },
    { name: "通讯", icon: "📱", color: "#3498DB", type: "EXPENSE", isDefault: true },
    { name: "医疗", icon: "🩺", color: "#E74C3C", type: "EXPENSE", isDefault: true },
    { name: "教育", icon: "📚", color: "#1ABC9C", type: "EXPENSE", isDefault: true },
    { name: "其他", icon: "🧾", color: "#95A5A6", type: "EXPENSE", isDefault: true },
    { name: "工资", icon: "💼", color: "#52C41A", type: "INCOME", isDefault: true },
    { name: "奖金", icon: "🏅", color: "#FAAD14", type: "INCOME", isDefault: true },
    { name: "投资", icon: "📈", color: "#722ED1", type: "INCOME", isDefault: true },
    { name: "兼职", icon: "🪙", color: "#13C2C2", type: "INCOME", isDefault: true },
    { name: "其他", icon: "💵", color: "#52C41A", type: "INCOME", isDefault: true },
];

const state = {
    data: loadData(),
    ui: {
        view: "bills",
        activeMonth: getCurrentMonth(),
        billFilterType: "ALL",
        statsPeriod: "month",
        statsType: "EXPENSE",
        mobileLayout: false,
        mobileToolsOpen: false,
        billForm: createEmptyBillForm("EXPENSE"),
        categoryForm: createEmptyCategoryForm("EXPENSE"),
        recurringForm: createEmptyRecurringForm("EXPENSE"),
        notice: null,
    },
    app: {
        mode: "local",
        firebaseConfigured: false,
        firebaseReady: false,
        firebaseReason: "",
        authUser: null,
        authMode: "login",
        authError: "",
        runningDueSync: false,
    },
};

init();

async function init() {
    processRecurringBills();
    ensureFormDefaults();
    syncViewportMode();
    bindEvents();
    await initSyncMode();
    render();
}

async function initSyncMode() {
    const service = window.BookkeepingFirebase;
    if (!service || typeof service.init !== "function") return;

    const result = service.init();
    state.app.firebaseConfigured = Boolean(result.configured);
    state.app.firebaseReady = Boolean(result.available);
    state.app.firebaseReason = result.reason || "";
    state.app.mode = result.available ? "firebase" : "local";

    if (!result.available) return;

    service.onAuthStateChanged(async function handleAuthState(user) {
        state.app.authUser = user;
        state.app.authError = "";
        state.app.runningDueSync = false;

        if (!user) {
            service.clearSubscriptions();
            render();
            return;
        }

        await service.ensureDefaultCategories(user.uid);
        service.subscribeUserData(user.uid, {
            onCategories: function(categories) {
                state.data.categories = categories;
                ensureFormDefaults();
                persist();
                render();
            },
            onBills: function(bills) {
                state.data.bills = bills;
                persist();
                render();
            },
            onRecurring: function(recurringBills) {
                state.data.recurringBills = recurringBills;
                persist();
                render();
                if (
                    recurringBills.some((item) => item.isActive && item.nextDueDate <= getToday()) &&
                    !state.app.runningDueSync
                ) {
                    state.app.runningDueSync = true;
                    void processRecurringBills().finally(function finishDueSync() {
                        state.app.runningDueSync = false;
                    });
                }
            },
        });
        render();
    });
}

function bindEvents() {
    document.addEventListener("click", handleClick);
    document.addEventListener("submit", handleSubmit);
    document.addEventListener("change", handleChange);
    window.addEventListener("resize", handleViewportChange, { passive: true });
    window.addEventListener("orientationchange", handleViewportChange, { passive: true });
}

function isMobileLayout() {
    if (typeof window === "undefined") return false;
    const screenWidth = window.screen && typeof window.screen.width === "number" ? window.screen.width : window.innerWidth;
    const screenHeight = window.screen && typeof window.screen.height === "number" ? window.screen.height : window.innerHeight;
    const shortSide = Math.min(screenWidth, screenHeight, window.innerWidth || screenWidth);
    const hasTouch = typeof navigator !== "undefined" && Number(navigator.maxTouchPoints || 0) > 0;
    return window.innerWidth <= 820 || (hasTouch && shortSide <= 1024);
}

function syncViewportMode() {
    const mobileLayout = isMobileLayout();
    state.ui.mobileLayout = mobileLayout;
    if (document.body) {
        document.body.classList.toggle("mobile-app", mobileLayout);
    }
}

function handleViewportChange() {
    const previous = state.ui.mobileLayout;
    syncViewportMode();
    if (previous !== state.ui.mobileLayout) {
        render();
    }
}

function handleClick(event) {
    const target = event.target.closest("[data-view-switch], [data-action], [data-role]");
    if (!target) return;

    if (target.dataset.viewSwitch) {
        state.ui.view = target.dataset.viewSwitch;
        state.ui.mobileToolsOpen = false;
        render();
        return;
    }

    if (target.dataset.action) {
        runAction(target.dataset.action, target.dataset);
        return;
    }

    if (target.dataset.role) {
        runRole(target.dataset.role, target.dataset);
    }
}

function handleSubmit(event) {
    const form = event.target;
    if (!(form instanceof HTMLFormElement)) return;

    if (form.id === "auth-form") {
        event.preventDefault();
        void submitAuthForm(form);
        return;
    }

    if (form.id === "bill-form") {
        event.preventDefault();
        void saveBill(form);
        return;
    }

    if (form.id === "category-form") {
        event.preventDefault();
        void saveCategory(form);
        return;
    }

    if (form.id === "recurring-form") {
        event.preventDefault();
        void saveRecurring(form);
    }
}

function handleChange(event) {
    const target = event.target;
    if (!(target instanceof HTMLElement)) return;

    if (target.id === "active-month") {
        state.ui.activeMonth = target.value || getCurrentMonth();
        render();
        return;
    }

    if (target.id === "import-file") {
        const file = target.files && target.files[0];
        state.ui.mobileToolsOpen = false;
        renderMobileToolbox();
        if (file) importData(file);
        target.value = "";
    }
}

function runAction(action, dataset) {
    if (state.ui.mobileLayout && action !== "toggle-mobile-tools" && action !== "close-mobile-tools") {
        state.ui.mobileToolsOpen = false;
    }

    switch (action) {
        case "toggle-auth-mode":
            state.app.authMode = state.app.authMode === "login" ? "register" : "login";
            state.app.authError = "";
            render();
            break;
        case "toggle-mobile-tools":
            state.ui.mobileToolsOpen = !state.ui.mobileToolsOpen;
            render();
            break;
        case "close-mobile-tools":
            state.ui.mobileToolsOpen = false;
            render();
            break;
        case "logout":
            void logoutCurrentUser();
            break;
        case "send-password-reset":
            void sendPasswordResetFromForm();
            break;
        case "export":
            exportData();
            break;
        case "export-csv":
            exportCsv();
            break;
        case "generate-due": {
            void runDueBillsGeneration();
            break;
        }
        case "restore-defaults":
            void restoreDefaultCategories();
            break;
        case "reset-bill-form":
            state.ui.billForm = createEmptyBillForm(state.ui.billForm.type);
            render();
            break;
        case "reset-category-form":
            state.ui.categoryForm = createEmptyCategoryForm(state.ui.categoryForm.type);
            render();
            break;
        case "reset-recurring-form":
            state.ui.recurringForm = createEmptyRecurringForm(state.ui.recurringForm.type);
            render();
            break;
        case "delete-bill":
            void removeBill(dataset.id);
            break;
        case "edit-bill":
            editBill(dataset.id);
            break;
        case "delete-category":
            void removeCategory(dataset.id);
            break;
        case "edit-category":
            editCategory(dataset.id);
            break;
        case "delete-recurring":
            void removeRecurring(dataset.id);
            break;
        case "edit-recurring":
            editRecurring(dataset.id);
            break;
        case "toggle-recurring":
            void toggleRecurring(dataset.id);
            break;
        default:
            break;
    }
}

function runRole(role, dataset) {
    switch (role) {
        case "bill-type":
            state.ui.billForm.type = dataset.value;
            state.ui.billForm.categoryId = getFirstCategoryId(dataset.value);
            render();
            break;
        case "category-type":
            state.ui.categoryForm.type = dataset.value;
            render();
            break;
        case "recurring-type":
            state.ui.recurringForm.type = dataset.value;
            state.ui.recurringForm.categoryId = getFirstCategoryId(dataset.value);
            render();
            break;
        case "bill-filter":
            state.ui.billFilterType = dataset.value;
            render();
            break;
        case "stats-period":
            state.ui.statsPeriod = dataset.value;
            render();
            break;
        case "stats-type":
            state.ui.statsType = dataset.value;
            render();
            break;
        default:
            break;
    }
}

function render() {
    ensureFormDefaults();
    syncViewportMode();
    renderAppShell();

    if (usingFirebase() && !state.app.authUser) {
        return;
    }

    const monthBills = getMonthBills(state.ui.activeMonth);
    const summary = getSummary(monthBills);
    const recurringDue = getDueRecurringCount();

    renderNotice();
    renderTabs();
    renderToolbar(summary.count, recurringDue);
    renderOverview(monthBills, summary);
    renderBills(monthBills);
    renderStats();
    renderCategories();
    renderRecurring();
    renderSidebar(monthBills, summary, recurringDue);
}

function renderAppShell() {
    document.body.classList.toggle("app-logged-in", Boolean(state.app.authUser));
    renderSessionPanel();
    renderAuthPanel();
    renderMobileNav();
    renderMobileToolbox();
}

function renderSessionPanel() {
    const panel = document.getElementById("session-panel");
    if (!panel) return;

    panel.classList.toggle("status-card--session", usingFirebase());

    if (usingFirebase()) {
        if (state.app.authUser) {
            panel.innerHTML = `
                <div class="session-card__main">
                    <span class="status-card__label">同步状态</span>
                    <strong class="status-card__value">Firebase 云端同步中</strong>
                    <p class="status-card__hint">${escapeHtml(state.app.authUser.email || state.app.authUser.uid)}</p>
                </div>
                <div class="session-card__actions">
                    <button class="button button--ghost button--compact" type="button" data-action="logout">退出</button>
                </div>
            `;
        } else {
            panel.innerHTML = `
                <div class="session-card__main">
                    <span class="status-card__label">同步状态</span>
                    <strong class="status-card__value">等待登录</strong>
                    <p class="status-card__hint">Firebase 已配置完成，登录后会和 App 共用同一套数据。</p>
                </div>
            `;
        }
    } else {
        panel.innerHTML = `
            <div class="session-card__main">
                <span class="status-card__label">运行模式</span>
                <strong class="status-card__value">本地模式</strong>
                <p class="status-card__hint">${escapeHtml(state.app.firebaseReason || "当前使用浏览器本地数据。")}</p>
            </div>
        `;
    }
}

function renderAuthPanel() {
    const panel = document.getElementById("auth-panel");
    const layout = document.querySelector(".layout");
    if (!panel || !layout) return;

    if (usingFirebase() && !state.app.authUser) {
        layout.classList.add("hidden");
        document.body.classList.remove("app-main-visible");
        panel.classList.remove("hidden");
        panel.innerHTML = `
            <div class="auth-grid">
                <article class="panel-card">
                    <h2 class="auth-title">${state.app.authMode === "login" ? "登录你的账本" : "创建网页端账号"}</h2>
                    <p class="auth-copy">
                        网页端会直接使用和 App 一样的 Firebase Authentication 与 Firestore 结构。
                        登录后，账单、分类、统计和周期账单都会实时同步。
                    </p>
                    ${state.app.authError ? `<div class="auth-error" style="margin-top: 16px;">${escapeHtml(state.app.authError)}</div>` : ""}
                    <form id="auth-form" class="field-group" style="margin-top: 18px;">
                        <label class="field">
                            <span>邮箱</span>
                            <input type="email" name="email" placeholder="you@example.com" required>
                        </label>
                        <label class="field">
                            <span>密码</span>
                            <input type="password" name="password" minlength="6" placeholder="至少 6 位" required>
                        </label>
                        ${state.app.authMode === "register" ? `
                            <label class="field">
                                <span>确认密码</span>
                                <input type="password" name="confirmPassword" minlength="6" placeholder="再次输入密码" required>
                            </label>
                        ` : ""}
                        <div class="form-actions">
                            <button class="button button--primary" type="submit">${state.app.authMode === "login" ? "登录" : "注册并同步"}</button>
                            <button class="button button--ghost" type="button" data-action="toggle-auth-mode">${state.app.authMode === "login" ? "去注册" : "去登录"}</button>
                            ${state.app.authMode === "login" ? `<button class="button button--ghost" type="button" data-action="send-password-reset">重置密码</button>` : ""}
                        </div>
                    </form>
                </article>

                <article class="panel-card">
                    <h2 class="auth-title">和 App 同步的前提</h2>
                    <ol class="steps">
                        <li>Firebase 控制台里创建一个 Web App。</li>
                        <li>把 Web 配置填进 <span class="mono">web/firebase-config.js</span>。</li>
                        <li>把 <span class="mono">enabled</span> 改成 <span class="mono">true</span>。</li>
                        <li>本地用 <span class="mono">http://localhost:8000</span> 打开网页端。</li>
                    </ol>
                    <p class="footer-note">如果你把 Web App 配置给我，我可以直接替你填好。</p>
                </article>
            </div>
        `;
        return;
    }

    if (state.app.firebaseConfigured && !state.app.firebaseReady) {
        layout.classList.remove("hidden");
        document.body.classList.add("app-main-visible");
        panel.classList.remove("hidden");
        panel.innerHTML = `
            <div class="auth-grid">
                <article class="panel-card">
                    <h2 class="auth-title">Firebase Web 还没配完整</h2>
                    <p class="auth-copy">${escapeHtml(state.app.firebaseReason || "当前还不能连接云端。")}</p>
                </article>
                <article class="panel-card">
                    <h2 class="auth-title">还差这一步</h2>
                    <ol class="steps">
                        <li>在 Firebase 控制台创建 Web App。</li>
                        <li>把配置填进 <span class="mono">web/firebase-config.js</span>。</li>
                        <li>把 <span class="mono">enabled</span> 改成 <span class="mono">true</span> 后刷新页面。</li>
                    </ol>
                </article>
            </div>
        `;
        return;
    }

    if (!state.app.firebaseConfigured) {
        layout.classList.remove("hidden");
        document.body.classList.add("app-main-visible");
        panel.classList.remove("hidden");
        panel.innerHTML = `
            <div class="auth-grid">
                <article class="panel-card">
                    <h2 class="auth-title">当前还是本地模式</h2>
                    <p class="auth-copy">
                        现在网页端功能可以先用，但不会和 App 同步。
                        仓库里只有 Android 的 Firebase 配置，还缺 Web App 的 <span class="mono">appId</span>。
                    </p>
                </article>
                <article class="panel-card">
                    <h2 class="auth-title">要开启同步</h2>
                    <ol class="steps">
                        <li>在 Firebase 控制台创建 Web App。</li>
                        <li>把控制台生成的配置填进 <span class="mono">web/firebase-config.js</span>。</li>
                        <li>把 <span class="mono">enabled</span> 改成 <span class="mono">true</span>。</li>
                    </ol>
                </article>
            </div>
        `;
        return;
    }

    layout.classList.remove("hidden");
    document.body.classList.add("app-main-visible");
    panel.classList.add("hidden");
    panel.innerHTML = "";
}

function usingFirebase() {
    return state.app.mode === "firebase" && state.app.firebaseReady;
}

async function submitAuthForm(form) {
    const service = window.BookkeepingFirebase;
    if (!usingFirebase() || !service) return;

    const formData = new FormData(form);
    const email = String(formData.get("email") || "").trim();
    const password = String(formData.get("password") || "");
    const confirmPassword = String(formData.get("confirmPassword") || "");

    if (state.app.authMode === "register" && password !== confirmPassword) {
        state.app.authError = "两次输入的密码不一致。";
        render();
        return;
    }

    try {
        state.app.authError = "";
        if (state.app.authMode === "login") {
            await service.login(email, password);
        } else {
            await service.register(email, password);
        }
    } catch (error) {
        state.app.authError = error instanceof Error ? error.message : "登录失败。";
        render();
    }
}

async function logoutCurrentUser() {
    const service = window.BookkeepingFirebase;
    if (!usingFirebase() || !service) return;
    await service.logout();
}

async function sendPasswordResetFromForm() {
    const service = window.BookkeepingFirebase;
    if (!usingFirebase() || !service) return;

    const emailInput = document.querySelector('#auth-form input[name="email"]');
    const email = emailInput instanceof HTMLInputElement ? emailInput.value.trim() : "";
    if (!email) {
        state.app.authError = "先输入需要重置密码的邮箱。";
        render();
        return;
    }

    try {
        await service.sendPasswordResetEmail(email);
        state.app.authError = "";
        setNotice(`重置密码邮件已发送到 ${email}，请检查收件箱和垃圾箱。`, "success");
        render();
    } catch (error) {
        state.app.authError = error instanceof Error ? error.message : "发送重置邮件失败。";
        render();
    }
}

async function runDueBillsGeneration() {
    const generated = await processRecurringBills();
    setNotice(generated > 0 ? `已生成 ${generated} 条到期账单。` : "当前没有到期的周期账单。", generated > 0 ? "success" : "warning");
    render();
}

function renderNotice() {
    const notice = document.getElementById("notice");
    if (!notice) return;

    if (!state.ui.notice) {
        notice.className = "notice notice--hidden";
        notice.textContent = "";
        return;
    }

    notice.className = `notice notice--${state.ui.notice.tone}`;
    notice.textContent = state.ui.notice.text;
}

function renderTabs() {
    document.querySelectorAll("[data-view-switch]").forEach((button) => {
        button.classList.toggle("is-active", button.dataset.viewSwitch === state.ui.view);
    });

    document.querySelectorAll(".view").forEach((section) => {
        section.classList.toggle("is-active", section.id === `view-${state.ui.view}`);
    });

    const monthInput = document.getElementById("active-month");
    if (monthInput) monthInput.value = state.ui.activeMonth;
}

function renderMobileNav() {
    const target = document.getElementById("mobile-nav");
    if (!target) return;

    const shouldShow = state.ui.mobileLayout && document.body.classList.contains("app-main-visible");
    if (!shouldShow) {
        target.className = "mobile-nav mobile-nav--hidden";
        target.innerHTML = "";
        return;
    }

    target.className = "mobile-nav";
    target.innerHTML = `
        <nav class="mobile-nav__bar" aria-label="主导航">
            <button class="tab" type="button" data-view-switch="overview"><span class="tab__icon">🏠</span><span class="tab__label">总览</span></button>
            <button class="tab" type="button" data-view-switch="bills"><span class="tab__icon">✍️</span><span class="tab__label">账单</span></button>
            <button class="tab" type="button" data-view-switch="stats"><span class="tab__icon">📈</span><span class="tab__label">统计</span></button>
            <button class="tab" type="button" data-view-switch="categories"><span class="tab__icon">🧩</span><span class="tab__label">分类</span></button>
            <button class="tab" type="button" data-view-switch="recurring"><span class="tab__icon">🔁</span><span class="tab__label">周期</span></button>
        </nav>
    `;
}

function renderToolbar(count, recurringDue) {
    const meta = document.getElementById("toolbar-meta");
    if (!meta) return;

    const statsText = state.ui.mobileLayout
        ? `${count} 条 · ${recurringDue} 待处理`
        : `${count} 条账单 · ${recurringDue} 条周期待处理`;

    meta.innerHTML = `
        <div class="toolbar-pill">
            <span class="toolbar-pill__month">${escapeHtml(formatMonthLabel(state.ui.activeMonth))}</span>
            <span class="toolbar-pill__stats">${statsText}</span>
        </div>
    `;
}

function renderOverview(monthBills, summary) {
    const target = document.getElementById("view-overview");
    if (!target) return;

    const expenseStats = getCategoryStats(monthBills, "EXPENSE");
    const incomeStats = getCategoryStats(monthBills, "INCOME");
    const groups = groupBillsByDate(monthBills);

    target.innerHTML = `
        <div class="summary-grid">
            ${renderMetricCard("本月收入", formatCurrency(summary.income), "metric-card--income", `${summary.incomeCount} 条收入记录`)}
            ${renderMetricCard("本月支出", formatCurrency(summary.expense), "metric-card--expense", `${summary.expenseCount} 条支出记录`)}
            ${renderMetricCard("结余", formatCurrency(summary.balance), "metric-card--balance", summary.balance >= 0 ? "当月现金流为正" : "当月现金流为负")}
            ${renderMetricCard("流水数量", String(summary.count), "metric-card--count", "浏览器本地保存")}
        </div>

        <div class="stats-grid" style="margin-top: 16px;">
            <article class="panel-card">
                <div class="section-head">
                    <div>
                        <h3>最近流水</h3>
                        <p>按日期倒序展示</p>
                    </div>
                </div>
                ${groups.length ? `<div class="list-stack">${groups.map(renderBillGroup).join("")}</div>` : renderEmpty("这个月还没有账单，去账单页录入第一笔。")}
            </article>

            <article class="panel-card">
                <div class="section-head">
                    <div>
                        <h3>支出热点</h3>
                        <p>按分类看本月支出结构</p>
                    </div>
                </div>
                ${expenseStats.length ? renderBarList(expenseStats.slice(0, 5)) : renderEmpty("暂无支出数据。")}
            </article>

            <article class="panel-card">
                <div class="section-head">
                    <div>
                        <h3>收入来源</h3>
                        <p>按分类看本月收入结构</p>
                    </div>
                </div>
                ${incomeStats.length ? renderBarList(incomeStats.slice(0, 4)) : renderEmpty("暂无收入数据。")}
            </article>
        </div>
    `;
}

function renderBills(monthBills) {
    const target = document.getElementById("view-bills");
    if (!target) return;

    const visibleBills = state.ui.billFilterType === "ALL"
        ? monthBills
        : monthBills.filter((bill) => bill.type === state.ui.billFilterType);
    const billCategories = getCategoriesByType(state.ui.billForm.type);
    const isEditing = Boolean(state.ui.billForm.id);

    target.innerHTML = `
        <div class="view-grid">
            <article class="panel-card">
                <div class="section-head">
                    <div>
                        <h3>${isEditing ? "编辑账单" : "新增账单"}</h3>
                        <p>直接沿用 Android 版的核心字段</p>
                    </div>
                </div>
                <form id="bill-form" class="field-group">
                    ${renderSegmented("bill-type", state.ui.billForm.type, [
                        { value: "EXPENSE", label: "支出", tone: "warm" },
                        { value: "INCOME", label: "收入", tone: "cool" },
                    ])}
                    <input type="hidden" name="type" value="${state.ui.billForm.type}">
                    <div class="form-grid">
                        <label class="field">
                            <span>金额</span>
                            <input type="number" name="amount" min="0" step="0.01" placeholder="0.00" value="${escapeAttribute(state.ui.billForm.amount)}" required>
                        </label>
                        <label class="field">
                            <span>分类</span>
                            <select name="categoryId" required>${renderCategoryOptions(billCategories, state.ui.billForm.categoryId)}</select>
                        </label>
                        <label class="field">
                            <span>日期</span>
                            <input type="date" name="billDate" value="${escapeAttribute(state.ui.billForm.billDate)}" required>
                        </label>
                    </div>
                    <label class="field">
                        <span>备注</span>
                        <textarea name="note" placeholder="例如：工作餐、打车、房租">${escapeHtml(state.ui.billForm.note)}</textarea>
                    </label>
                    <div class="form-actions">
                        <button class="button button--primary" type="submit">${isEditing ? "更新账单" : "保存账单"}</button>
                        <button class="button button--ghost" type="button" data-action="reset-bill-form">清空表单</button>
                    </div>
                </form>
            </article>

            <article class="panel-card">
                <div class="section-head">
                    <div>
                        <h3>账单列表</h3>
                        <p>${escapeHtml(formatMonthLabel(state.ui.activeMonth))} 的全部流水</p>
                    </div>
                    ${renderSegmented("bill-filter", state.ui.billFilterType, [
                        { value: "ALL", label: "全部" },
                        { value: "EXPENSE", label: "支出" },
                        { value: "INCOME", label: "收入" },
                    ])}
                </div>
                ${visibleBills.length ? `<div class="list-stack">${visibleBills.map(renderBillRow).join("")}</div>` : renderEmpty("当前筛选条件下没有账单。")}
            </article>
        </div>
    `;
}

function renderStats() {
    const target = document.getElementById("view-stats");
    if (!target) return;

    const periodBills = state.ui.statsPeriod === "month"
        ? getMonthBills(state.ui.activeMonth)
        : getYearBills(state.ui.activeMonth.slice(0, 4));
    const summary = getSummary(periodBills);
    const categoryStats = getCategoryStats(periodBills, state.ui.statsType);
    const scopeLabel = state.ui.statsPeriod === "month"
        ? formatMonthLabel(state.ui.activeMonth)
        : `${state.ui.activeMonth.slice(0, 4)} 年`;

    target.innerHTML = `
        <div class="panel-card">
            <div class="section-head">
                <div>
                    <h3>统计视图</h3>
                    <p>按月或按年查看收入与支出结构</p>
                </div>
                <div class="inline-actions">
                    ${renderSegmented("stats-period", state.ui.statsPeriod, [
                        { value: "month", label: "按月" },
                        { value: "year", label: "按年" },
                    ])}
                    ${renderSegmented("stats-type", state.ui.statsType, [
                        { value: "EXPENSE", label: "支出" },
                        { value: "INCOME", label: "收入" },
                    ])}
                </div>
            </div>
            <div class="summary-grid">
                ${renderMetricCard(`${scopeLabel}收入`, formatCurrency(summary.income), "metric-card--income", `${summary.incomeCount} 条收入`)}
                ${renderMetricCard(`${scopeLabel}支出`, formatCurrency(summary.expense), "metric-card--expense", `${summary.expenseCount} 条支出`)}
                ${renderMetricCard(`${scopeLabel}净额`, formatCurrency(summary.balance), "metric-card--balance", state.ui.statsPeriod === "month" ? "按月计算" : "按年计算")}
            </div>
        </div>

        <div class="stats-grid" style="margin-top: 16px;">
            <article class="panel-card">
                <div class="section-head">
                    <div>
                        <h3>${scopeLabel}${BILL_TYPE_LABELS[state.ui.statsType]}分类分布</h3>
                        <p>按金额从高到低排序</p>
                    </div>
                </div>
                ${categoryStats.length ? renderBarList(categoryStats) : renderEmpty("当前统计范围没有数据。")}
            </article>

            <article class="panel-card">
                <div class="section-head">
                    <div>
                        <h3>重点结论</h3>
                        <p>给你一个快速判断视角</p>
                    </div>
                </div>
                ${renderStatsInsights(summary, categoryStats, scopeLabel)}
            </article>
        </div>
    `;
}

function renderCategories() {
    const target = document.getElementById("view-categories");
    if (!target) return;

    const expenseCategories = getCategoriesByType("EXPENSE");
    const incomeCategories = getCategoriesByType("INCOME");
    const isEditing = Boolean(state.ui.categoryForm.id);

    target.innerHTML = `
        <div class="view-grid">
            <article class="panel-card">
                <div class="section-head">
                    <div>
                        <h3>${isEditing ? "编辑分类" : "新增分类"}</h3>
                        <p>图标支持 emoji，颜色会用于统计和卡片展示</p>
                    </div>
                </div>
                <form id="category-form" class="field-group">
                    ${renderSegmented("category-type", state.ui.categoryForm.type, [
                        { value: "EXPENSE", label: "支出分类", tone: "warm" },
                        { value: "INCOME", label: "收入分类", tone: "cool" },
                    ])}
                    <input type="hidden" name="type" value="${state.ui.categoryForm.type}">
                    <div class="form-grid">
                        <label class="field">
                            <span>名称</span>
                            <input type="text" name="name" maxlength="20" placeholder="例如：宠物、差旅" value="${escapeAttribute(state.ui.categoryForm.name)}" required>
                        </label>
                        <label class="field">
                            <span>图标</span>
                            <input type="text" name="icon" maxlength="4" placeholder="🐾" value="${escapeAttribute(state.ui.categoryForm.icon)}" required>
                        </label>
                        <label class="field">
                            <span>颜色</span>
                            <input type="color" name="color" value="${escapeAttribute(state.ui.categoryForm.color)}">
                        </label>
                    </div>
                    <div class="form-actions">
                        <button class="button button--primary" type="submit">${isEditing ? "更新分类" : "保存分类"}</button>
                        <button class="button button--ghost" type="button" data-action="reset-category-form">清空表单</button>
                    </div>
                </form>
            </article>

            <div class="category-grid">
                <article class="panel-card">
                    <div class="section-head">
                        <div>
                            <h3>支出分类</h3>
                            <p>${expenseCategories.length} 个分类</p>
                        </div>
                    </div>
                    ${expenseCategories.length ? `<div class="list-stack">${expenseCategories.map(renderCategoryRow).join("")}</div>` : renderEmpty("暂无支出分类。")}
                </article>

                <article class="panel-card">
                    <div class="section-head">
                        <div>
                            <h3>收入分类</h3>
                            <p>${incomeCategories.length} 个分类</p>
                        </div>
                    </div>
                    ${incomeCategories.length ? `<div class="list-stack">${incomeCategories.map(renderCategoryRow).join("")}</div>` : renderEmpty("暂无收入分类。")}
                </article>
            </div>
        </div>
    `;
}

function renderRecurring() {
    const target = document.getElementById("view-recurring");
    if (!target) return;

    const recurringCategories = getCategoriesByType(state.ui.recurringForm.type);
    const items = [...state.data.recurringBills].sort((a, b) => a.nextDueDate.localeCompare(b.nextDueDate));
    const isEditing = Boolean(state.ui.recurringForm.id);

    target.innerHTML = `
        <div class="view-grid">
            <article class="panel-card">
                <div class="section-head">
                    <div>
                        <h3>${isEditing ? "编辑周期账单" : "新增周期账单"}</h3>
                        <p>打开页面时会自动补生成到期账单</p>
                    </div>
                </div>
                <form id="recurring-form" class="field-group">
                    ${renderSegmented("recurring-type", state.ui.recurringForm.type, [
                        { value: "EXPENSE", label: "周期支出", tone: "warm" },
                        { value: "INCOME", label: "周期收入", tone: "cool" },
                    ])}
                    <input type="hidden" name="type" value="${state.ui.recurringForm.type}">
                    <div class="form-grid">
                        <label class="field">
                            <span>金额</span>
                            <input type="number" name="amount" min="0" step="0.01" placeholder="0.00" value="${escapeAttribute(state.ui.recurringForm.amount)}" required>
                        </label>
                        <label class="field">
                            <span>分类</span>
                            <select name="categoryId" required>${renderCategoryOptions(recurringCategories, state.ui.recurringForm.categoryId)}</select>
                        </label>
                        <label class="field">
                            <span>频率</span>
                            <select name="frequency">${renderFrequencyOptions(state.ui.recurringForm.frequency)}</select>
                        </label>
                        <label class="field">
                            <span>开始日期</span>
                            <input type="date" name="startDate" value="${escapeAttribute(state.ui.recurringForm.startDate)}" required>
                        </label>
                        <label class="field">
                            <span>是否启用</span>
                            <select name="isActive">
                                <option value="true" ${state.ui.recurringForm.isActive ? "selected" : ""}>启用</option>
                                <option value="false" ${state.ui.recurringForm.isActive ? "" : "selected"}>停用</option>
                            </select>
                        </label>
                    </div>
                    <label class="field">
                        <span>备注</span>
                        <textarea name="note" placeholder="例如：房租、会员费、工资">${escapeHtml(state.ui.recurringForm.note)}</textarea>
                    </label>
                    <div class="form-actions">
                        <button class="button button--primary" type="submit">${isEditing ? "更新周期账单" : "保存周期账单"}</button>
                        <button class="button button--ghost" type="button" data-action="reset-recurring-form">清空表单</button>
                    </div>
                </form>
            </article>

            <article class="panel-card">
                <div class="section-head">
                    <div>
                        <h3>周期账单列表</h3>
                        <p>${items.length} 条规则，按下次执行时间排序</p>
                    </div>
                </div>
                ${items.length ? `<div class="list-stack">${items.map(renderRecurringRow).join("")}</div>` : renderEmpty("还没有周期账单。")}
            </article>
        </div>
    `;
}

function renderSidebar(monthBills, summary, recurringDue) {
    const target = document.getElementById("sidebar");
    if (!target) return;

    if (state.ui.mobileLayout) {
        target.innerHTML = "";
        target.classList.add("hidden");
        return;
    }

    target.classList.remove("hidden");

    const topExpense = getCategoryStats(monthBills, "EXPENSE")[0];
    const topIncome = getCategoryStats(monthBills, "INCOME")[0];
    const nextRecurring = [...state.data.recurringBills]
        .filter((item) => item.isActive)
        .sort((a, b) => a.nextDueDate.localeCompare(b.nextDueDate))[0];

    target.innerHTML = `
        <div class="sidebar-stack">
            <article class="sidebar-card">
                <div class="sidebar-card__top">
                    <div>
                        <h3>数据概况</h3>
                        <p class="muted-text">当前浏览器中的本地账本</p>
                    </div>
                </div>
                <ul>
                    <li><span>账单总数</span><strong>${state.data.bills.length}</strong></li>
                    <li><span>分类总数</span><strong>${state.data.categories.length}</strong></li>
                    <li><span>周期账单</span><strong>${state.data.recurringBills.length}</strong></li>
                    <li><span>最后保存</span><strong>${escapeHtml(formatDateTime(state.data.updatedAt))}</strong></li>
                </ul>
            </article>

            <article class="sidebar-card">
                <div class="sidebar-card__top">
                    <div>
                        <h3>本月判断</h3>
                        <p class="muted-text">${escapeHtml(formatMonthLabel(state.ui.activeMonth))}</p>
                    </div>
                </div>
                <ul>
                    <li><span>现金流</span><strong>${summary.balance >= 0 ? "净流入" : "净流出"}</strong></li>
                    <li><span>最大支出分类</span><strong>${escapeHtml(topExpense ? `${topExpense.category.icon} ${topExpense.category.name}` : "暂无")}</strong></li>
                    <li><span>最大收入来源</span><strong>${escapeHtml(topIncome ? `${topIncome.category.icon} ${topIncome.category.name}` : "暂无")}</strong></li>
                    <li><span>待处理周期</span><strong>${recurringDue}</strong></li>
                </ul>
            </article>

            <article class="sidebar-card">
                <div class="sidebar-card__top">
                    <div>
                        <h3>周期提醒</h3>
                        <p class="muted-text">自动生成遵循当前本地日期</p>
                    </div>
                </div>
                ${nextRecurring ? `
                    <div class="chip-row">
                        <span class="chip chip--accent">下一条：${escapeHtml(nextRecurring.nextDueDate)}</span>
                        <span class="chip ${nextRecurring.isActive ? "chip--success" : "chip--danger"}">${nextRecurring.isActive ? "启用中" : "已停用"}</span>
                    </div>
                    <p class="footer-note">${escapeHtml(nextRecurring.categoryIcon)} ${escapeHtml(nextRecurring.categoryName)} · ${formatCurrency(nextRecurring.amount)} · ${escapeHtml(FREQUENCY_LABELS[nextRecurring.frequency])}</p>
                ` : renderEmpty("当前没有启用中的周期账单。")}
            </article>

            <article class="sidebar-card">
                <div class="sidebar-card__top">
                    <div>
                        <h3>说明</h3>
                    </div>
                </div>
                <p class="footer-note">
                    这个 Web 版先使用浏览器本地存储，不会自动同步 Android 端 Firebase 数据。
                    如果你要同库同步，下一步需要补 Firebase Web App 配置和授权域名。
                </p>
            </article>
        </div>
    `;
}

function renderMobileToolbox() {
    const target = document.getElementById("mobile-toolbox");
    if (!target) return;

    const shouldShow = state.ui.mobileLayout && document.body.classList.contains("app-main-visible");
    if (!shouldShow) {
        state.ui.mobileToolsOpen = false;
        target.className = "mobile-toolbox mobile-toolbox--hidden";
        target.innerHTML = "";
        return;
    }

    const statusLabel = usingFirebase() ? "同步状态" : "运行模式";
    const statusTitle = usingFirebase()
        ? (state.app.authUser ? "Firebase 云端同步中" : "等待登录")
        : "本地模式";
    const statusHint = usingFirebase()
        ? (state.app.authUser ? (state.app.authUser.email || state.app.authUser.uid) : "登录后会和 App 共用同一套数据。")
        : (state.app.firebaseReason || "当前使用浏览器本地数据。");

    target.className = `mobile-toolbox${state.ui.mobileToolsOpen ? " is-open" : ""}`;
    target.innerHTML = `
        <button class="button button--primary mobile-toolbox__toggle" type="button" data-action="toggle-mobile-tools">
            ${state.ui.mobileToolsOpen ? "收起工具" : "工具"}
        </button>
        <button class="mobile-toolbox__backdrop" type="button" data-action="close-mobile-tools" aria-label="关闭工具面板"></button>
        <section class="mobile-toolbox__sheet" aria-label="底部工具面板">
            <div class="mobile-toolbox__sheet-head">
                <div>
                    <span class="mobile-toolbox__label">${escapeHtml(statusLabel)}</span>
                    <strong class="mobile-toolbox__title">${escapeHtml(statusTitle)}</strong>
                    <p class="mobile-toolbox__hint">${escapeHtml(statusHint)}</p>
                </div>
                <div class="mobile-toolbox__sheet-actions">
                    ${state.app.authUser ? `<button class="button button--ghost button--compact" type="button" data-action="logout">退出</button>` : ""}
                    <button class="button button--ghost button--compact" type="button" data-action="close-mobile-tools">关闭</button>
                </div>
            </div>
            <div class="mobile-toolbox__grid">
                <button class="button button--primary" type="button" data-action="export">导出 JSON</button>
                <button class="button button--ghost" type="button" data-action="export-csv">导出 CSV</button>
                <label class="button button--secondary" for="import-file">导入 JSON</label>
                <button class="button button--ghost" type="button" data-action="generate-due">生成到期账单</button>
                <button class="button button--ghost" type="button" data-action="restore-defaults">恢复默认分类</button>
            </div>
        </section>
    `;
}

function renderMetricCard(label, value, modifier, subtle) {
    return `
        <article class="metric-card ${modifier}">
            <span class="metric-card__label">${escapeHtml(label)}</span>
            <strong class="metric-card__value">${escapeHtml(value)}</strong>
            <span class="metric-card__subtle">${escapeHtml(subtle)}</span>
        </article>
    `;
}

function renderEmpty(text) {
    return `<div class="empty-state">${escapeHtml(text)}</div>`;
}

function renderSegmented(name, selected, options) {
    const role = name;
    return `
        <div class="segmented" aria-label="${escapeAttribute(name)}">
            ${options.map((option) => `
                <button
                    type="button"
                    class="segmented__option ${selected === option.value ? "is-active" : ""}"
                    data-role="${escapeAttribute(role)}"
                    data-value="${escapeAttribute(option.value)}"
                    ${option.tone ? `data-tone="${escapeAttribute(option.tone)}"` : ""}
                >${escapeHtml(option.label)}</button>
            `).join("")}
        </div>
    `;
}

function renderCategoryOptions(categories, selectedId) {
    return categories.map((category) => `
        <option value="${escapeAttribute(category.id)}" ${category.id === selectedId ? "selected" : ""}>
            ${escapeHtml(category.icon)} ${escapeHtml(category.name)}
        </option>
    `).join("");
}

function renderFrequencyOptions(selected) {
    return Object.entries(FREQUENCY_LABELS).map(([value, label]) => `
        <option value="${escapeAttribute(value)}" ${value === selected ? "selected" : ""}>${escapeHtml(label)}</option>
    `).join("");
}

function renderBillGroup(group) {
    return `
        <section>
            <div class="section-head">
                <div>
                    <h4>${escapeHtml(formatDateLabel(group.date))}</h4>
                    <p>${group.items.length} 条账单</p>
                </div>
            </div>
            <div class="list-stack">${group.items.slice(0, 4).map(renderBillRow).join("")}</div>
        </section>
    `;
}

function renderBillRow(bill) {
    return `
        <article class="record-row">
            <div class="record-row__top">
                <div class="record-row__main">
                    <span class="icon-pill" style="background:${escapeAttribute(withOpacity(bill.categoryColor, 0.16))};">${escapeHtml(bill.categoryIcon)}</span>
                    <div>
                        <p class="record-row__title">${escapeHtml(bill.categoryName)}</p>
                        <p class="record-row__meta">${escapeHtml(formatDateLabel(bill.billDate))}${bill.note ? ` · ${escapeHtml(bill.note)}` : ""}</p>
                    </div>
                </div>
                <div class="record-row__side">
                    <div class="amount ${bill.type === "EXPENSE" ? "amount--expense" : "amount--income"}">${bill.type === "EXPENSE" ? "-" : "+"}${escapeHtml(formatCurrency(bill.amount))}</div>
                    <div class="record-row__actions">
                        <button class="button button--ghost button--compact" type="button" data-action="edit-bill" data-id="${escapeAttribute(bill.id)}">编辑</button>
                        <button class="button button--danger button--compact" type="button" data-action="delete-bill" data-id="${escapeAttribute(bill.id)}">删除</button>
                    </div>
                </div>
            </div>
            <div class="chip-row">
                <span class="chip">${escapeHtml(BILL_TYPE_LABELS[bill.type])}</span>
                <span class="chip">${escapeHtml(bill.billDate)}</span>
            </div>
        </article>
    `;
}

function renderCategoryRow(category) {
    const usage = state.data.bills.filter((bill) => bill.categoryId === category.id).length;
    const recurringUsage = state.data.recurringBills.filter((item) => item.categoryId === category.id).length;

    return `
        <article class="category-card">
            <div class="category-card__top">
                <div class="category-card__main">
                    <span class="icon-pill" style="background:${escapeAttribute(withOpacity(category.color, 0.16))};">${escapeHtml(category.icon)}</span>
                    <div>
                        <p class="category-card__title">${escapeHtml(category.name)}</p>
                        <p class="category-card__meta">${category.isDefault ? "默认分类" : "自定义分类"} · ${usage} 笔账单 · ${recurringUsage} 条周期规则</p>
                    </div>
                </div>
                <div class="category-card__actions">
                    <button class="button button--ghost button--compact" type="button" data-action="edit-category" data-id="${escapeAttribute(category.id)}">编辑</button>
                    <button class="button button--danger button--compact" type="button" data-action="delete-category" data-id="${escapeAttribute(category.id)}">删除</button>
                </div>
            </div>
            <div class="chip-row">
                <span class="chip">${escapeHtml(BILL_TYPE_LABELS[category.type])}</span>
                <span class="chip">${escapeHtml(category.color)}</span>
            </div>
        </article>
    `;
}

function renderRecurringRow(item) {
    const statusClass = item.isActive ? "chip--success" : "chip--danger";
    return `
        <article class="recurring-card">
            <div class="recurring-card__top">
                <div class="recurring-card__main">
                    <span class="icon-pill" style="background:${escapeAttribute(withOpacity(item.categoryColor, 0.16))};">${escapeHtml(item.categoryIcon)}</span>
                    <div>
                        <p class="recurring-card__title">${escapeHtml(item.categoryName)} · ${escapeHtml(FREQUENCY_LABELS[item.frequency])}</p>
                        <p class="recurring-card__meta">起始于 ${escapeHtml(item.startDate)}，下次执行 ${escapeHtml(item.nextDueDate)}${item.note ? ` · ${escapeHtml(item.note)}` : ""}</p>
                    </div>
                </div>
                <div class="recurring-card__side">
                    <div class="amount ${item.type === "EXPENSE" ? "amount--expense" : "amount--income"}">${item.type === "EXPENSE" ? "-" : "+"}${escapeHtml(formatCurrency(item.amount))}</div>
                    <div class="recurring-card__actions">
                        <button class="button button--ghost button--compact" type="button" data-action="toggle-recurring" data-id="${escapeAttribute(item.id)}">${item.isActive ? "停用" : "启用"}</button>
                        <button class="button button--ghost button--compact" type="button" data-action="edit-recurring" data-id="${escapeAttribute(item.id)}">编辑</button>
                        <button class="button button--danger button--compact" type="button" data-action="delete-recurring" data-id="${escapeAttribute(item.id)}">删除</button>
                    </div>
                </div>
            </div>
            <div class="chip-row">
                <span class="chip ${statusClass}">${item.isActive ? "启用中" : "已停用"}</span>
                <span class="chip">${escapeHtml(BILL_TYPE_LABELS[item.type])}</span>
                <span class="chip chip--accent">${escapeHtml(item.nextDueDate)}</span>
            </div>
        </article>
    `;
}

function renderBarList(items) {
    return `
        <div class="stats-list">
            ${items.map((item) => `
                <article class="bar-card">
                    <div class="bar-card__top">
                        <strong>${escapeHtml(item.category.icon)} ${escapeHtml(item.category.name)}</strong>
                        <span>${escapeHtml(formatCurrency(item.amount))} · ${Math.round(item.percentage * 100)}%</span>
                    </div>
                    <div class="bar"><span style="width:${Math.max(item.percentage * 100, 4)}%; background:${escapeAttribute(item.category.color)};"></span></div>
                </article>
            `).join("")}
        </div>
    `;
}

function renderStatsInsights(summary, categoryStats, scopeLabel) {
    const top = categoryStats[0];
    const second = categoryStats[1];
    const hints = [
        `${scopeLabel}共记录 ${summary.count} 笔账单，净额为 ${formatCurrency(summary.balance)}。`,
        top ? `占比最高的是 ${top.category.icon} ${top.category.name}，金额 ${formatCurrency(top.amount)}，占 ${Math.round(top.percentage * 100)}%。` : "当前范围还没有形成分类分布。",
        second ? `第二位是 ${second.category.icon} ${second.category.name}，说明消费或收入来源已经开始集中。` : "目前只有一个主要分类，结构还比较单一。",
    ];

    return `<div class="list-stack">${hints.map((hint) => `<div class="bar-card">${escapeHtml(hint)}</div>`).join("")}</div>`;
}

async function saveBill(form) {
    const formData = new FormData(form);
    const amount = Number(formData.get("amount"));
    const type = String(formData.get("type"));
    const categoryId = String(formData.get("categoryId"));
    const category = state.data.categories.find((item) => item.id === categoryId);
    const isEditing = Boolean(state.ui.billForm.id);

    if (!Number.isFinite(amount) || amount <= 0) {
        setNotice("账单金额必须大于 0。", "danger");
        render();
        return;
    }

    if (!category) {
        setNotice("请选择一个有效分类。", "danger");
        render();
        return;
    }

    const bill = {
        id: state.ui.billForm.id || createId("bill"),
        amount: roundCurrency(amount),
        type,
        categoryId: category.id,
        categoryName: category.name,
        categoryIcon: category.icon,
        categoryColor: category.color,
        note: String(formData.get("note") || "").trim(),
        billDate: String(formData.get("billDate")),
        createdAt: state.ui.billForm.createdAt || new Date().toISOString(),
    };

    if (usingFirebase() && state.app.authUser) {
        try {
            await window.BookkeepingFirebase.saveBill(state.app.authUser.uid, bill);
            setNotice(isEditing ? "账单已同步到云端。" : "账单已保存并同步。", "success");
            state.ui.billForm = createEmptyBillForm(type);
        } catch (error) {
            setNotice(error instanceof Error ? error.message : "账单同步失败。", "danger");
        }
        render();
        return;
    }

    const existingIndex = state.data.bills.findIndex((item) => item.id === bill.id);
    if (existingIndex >= 0) {
        state.data.bills[existingIndex] = bill;
        setNotice("账单已更新。", "success");
    } else {
        state.data.bills.push(bill);
        setNotice("账单已保存。", "success");
    }

    persist();
    state.ui.billForm = createEmptyBillForm(type);
    render();
}

async function saveCategory(form) {
    const formData = new FormData(form);
    const name = String(formData.get("name") || "").trim();
    const icon = String(formData.get("icon") || "").trim();
    const color = String(formData.get("color") || "#4A90D9");
    const type = String(formData.get("type") || "EXPENSE");
    const isEditing = Boolean(state.ui.categoryForm.id);

    if (!name || !icon) {
        setNotice("分类名称和图标都不能为空。", "danger");
        render();
        return;
    }

    const duplicate = state.data.categories.find((item) =>
        item.type === type &&
        item.name === name &&
        item.id !== state.ui.categoryForm.id
    );
    if (duplicate) {
        setNotice("同类型下已经存在同名分类。", "danger");
        render();
        return;
    }

    if (state.ui.categoryForm.id) {
        const current = state.data.categories.find((item) => item.id === state.ui.categoryForm.id);
        const usage = state.data.bills.filter((bill) => bill.categoryId === state.ui.categoryForm.id).length;
        const recurringUsage = state.data.recurringBills.filter((item) => item.categoryId === state.ui.categoryForm.id).length;
        if (current && current.type !== type && (usage > 0 || recurringUsage > 0)) {
            setNotice("已被账单或周期账单使用的分类不能直接切换收入/支出类型，请新建一个分类。", "danger");
            render();
            return;
        }
    }

    const category = {
        id: state.ui.categoryForm.id || createId("category"),
        name,
        icon,
        color,
        type,
        isDefault: state.ui.categoryForm.isDefault || false,
    };

    if (usingFirebase() && state.app.authUser) {
        try {
            await window.BookkeepingFirebase.saveCategory(state.app.authUser.uid, category);
            setNotice(isEditing ? "分类已同步到云端。" : "分类已保存并同步。", "success");
            state.ui.categoryForm = createEmptyCategoryForm(type);
        } catch (error) {
            setNotice(error instanceof Error ? error.message : "分类同步失败。", "danger");
        }
        render();
        return;
    }

    const existingIndex = state.data.categories.findIndex((item) => item.id === category.id);
    if (existingIndex >= 0) {
        state.data.categories[existingIndex] = category;
        setNotice("分类已更新。", "success");
    } else {
        state.data.categories.push(category);
        setNotice("分类已保存。", "success");
    }

    persist();
    state.ui.categoryForm = createEmptyCategoryForm(type);
    ensureFormDefaults();
    render();
}

async function saveRecurring(form) {
    const formData = new FormData(form);
    const amount = Number(formData.get("amount"));
    const type = String(formData.get("type") || "EXPENSE");
    const categoryId = String(formData.get("categoryId"));
    const category = state.data.categories.find((item) => item.id === categoryId);
    const startDate = String(formData.get("startDate"));
    const frequency = String(formData.get("frequency") || "MONTHLY");
    const isActive = String(formData.get("isActive")) === "true";
    const isEditing = Boolean(state.ui.recurringForm.id);

    if (!Number.isFinite(amount) || amount <= 0) {
        setNotice("周期账单金额必须大于 0。", "danger");
        render();
        return;
    }

    if (!category) {
        setNotice("请选择一个有效分类。", "danger");
        render();
        return;
    }

    const existingItem = state.data.recurringBills.find((item) => item.id === state.ui.recurringForm.id);
    const recurring = {
        id: state.ui.recurringForm.id || createId("recurring"),
        amount: roundCurrency(amount),
        type,
        categoryId: category.id,
        categoryName: category.name,
        categoryIcon: category.icon,
        categoryColor: category.color,
        note: String(formData.get("note") || "").trim(),
        frequency,
        startDate,
        nextDueDate: existingItem ? existingItem.nextDueDate : startDate,
        isActive,
        createdAt: state.ui.recurringForm.createdAt || new Date().toISOString(),
    };

    if (usingFirebase() && state.app.authUser) {
        try {
            await window.BookkeepingFirebase.saveRecurring(state.app.authUser.uid, recurring);
            state.ui.recurringForm = createEmptyRecurringForm(type);
            setNotice(isEditing ? "周期账单已同步到云端。" : "周期账单已保存并同步。", "success");
            await processRecurringBills();
        } catch (error) {
            setNotice(error instanceof Error ? error.message : "周期账单同步失败。", "danger");
        }
        render();
        return;
    }

    const existingIndex = state.data.recurringBills.findIndex((item) => item.id === recurring.id);
    if (existingIndex >= 0) {
        state.data.recurringBills[existingIndex] = recurring;
        setNotice("周期账单已更新。", "success");
    } else {
        state.data.recurringBills.push(recurring);
        setNotice("周期账单已保存。", "success");
    }

    persist();
    state.ui.recurringForm = createEmptyRecurringForm(type);
    processRecurringBills();
    render();
}

async function removeBill(id) {
    const bill = state.data.bills.find((item) => item.id === id);
    if (!bill) return;
    if (!window.confirm(`确认删除这笔${BILL_TYPE_LABELS[bill.type]}账单吗？`)) return;

    if (usingFirebase() && state.app.authUser) {
        try {
            await window.BookkeepingFirebase.deleteBill(state.app.authUser.uid, id);
            setNotice("账单已从云端删除。", "warning");
        } catch (error) {
            setNotice(error instanceof Error ? error.message : "删除账单失败。", "danger");
        }
        render();
        return;
    }

    state.data.bills = state.data.bills.filter((item) => item.id !== id);
    persist();
    setNotice("账单已删除。", "warning");
    render();
}

function editBill(id) {
    const bill = state.data.bills.find((item) => item.id === id);
    if (!bill) return;

    state.ui.view = "bills";
    state.ui.billForm = {
        id: bill.id,
        type: bill.type,
        amount: String(bill.amount),
        categoryId: bill.categoryId,
        note: bill.note || "",
        billDate: bill.billDate,
        createdAt: bill.createdAt,
    };
    setNotice("已载入账单内容，可以直接修改。", "warning");
    render();
}

async function removeCategory(id) {
    const category = state.data.categories.find((item) => item.id === id);
    if (!category) return;

    if (getCategoriesByType(category.type).length <= 1) {
        setNotice(`至少保留一个${BILL_TYPE_LABELS[category.type]}分类。`, "danger");
        render();
        return;
    }

    const usage = state.data.bills.filter((bill) => bill.categoryId === id).length;
    const recurringUsage = state.data.recurringBills.filter((item) => item.categoryId === id).length;
    const message = usage || recurringUsage
        ? `这个分类已关联 ${usage} 笔账单和 ${recurringUsage} 条周期规则，删除后历史记录会保留原文案，但以后不能继续选用。确认删除吗？`
        : "确认删除这个分类吗？";

    if (!window.confirm(message)) return;

    if (usingFirebase() && state.app.authUser) {
        try {
            await window.BookkeepingFirebase.deleteCategory(state.app.authUser.uid, id);
            setNotice("分类已从云端删除。", "warning");
        } catch (error) {
            setNotice(error instanceof Error ? error.message : "删除分类失败。", "danger");
        }
        render();
        return;
    }

    state.data.categories = state.data.categories.filter((item) => item.id !== id);
    persist();
    ensureFormDefaults();
    setNotice("分类已删除。", "warning");
    render();
}

function editCategory(id) {
    const category = state.data.categories.find((item) => item.id === id);
    if (!category) return;

    state.ui.view = "categories";
    state.ui.categoryForm = { ...category };
    setNotice("已载入分类内容，可以继续编辑。", "warning");
    render();
}

async function removeRecurring(id) {
    if (!window.confirm("确认删除这条周期账单吗？")) return;

    if (usingFirebase() && state.app.authUser) {
        try {
            await window.BookkeepingFirebase.deleteRecurring(state.app.authUser.uid, id);
            setNotice("周期账单已从云端删除。", "warning");
        } catch (error) {
            setNotice(error instanceof Error ? error.message : "删除周期账单失败。", "danger");
        }
        render();
        return;
    }

    state.data.recurringBills = state.data.recurringBills.filter((item) => item.id !== id);
    persist();
    setNotice("周期账单已删除。", "warning");
    render();
}

function editRecurring(id) {
    const item = state.data.recurringBills.find((recurring) => recurring.id === id);
    if (!item) return;

    state.ui.view = "recurring";
    state.ui.recurringForm = {
        id: item.id,
        type: item.type,
        amount: String(item.amount),
        categoryId: item.categoryId,
        note: item.note || "",
        frequency: item.frequency,
        startDate: item.startDate,
        isActive: item.isActive,
        createdAt: item.createdAt,
    };
    setNotice("已载入周期账单内容，可以继续编辑。", "warning");
    render();
}

async function toggleRecurring(id) {
    const item = state.data.recurringBills.find((recurring) => recurring.id === id);
    if (!item) return;

    if (usingFirebase() && state.app.authUser) {
        try {
            await window.BookkeepingFirebase.toggleRecurring(state.app.authUser.uid, item);
            setNotice(item.isActive ? "周期账单已停用并同步。" : "周期账单已启用并同步。", item.isActive ? "warning" : "success");
        } catch (error) {
            setNotice(error instanceof Error ? error.message : "更新周期账单失败。", "danger");
        }
        render();
        return;
    }

    item.isActive = !item.isActive;
    persist();
    setNotice(item.isActive ? "周期账单已启用。" : "周期账单已停用。", item.isActive ? "success" : "warning");
    render();
}

function exportData() {
    const payload = JSON.stringify(state.data, null, 2);
    const blob = new Blob([payload], { type: "application/json" });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = `bookkeeping-web-${state.ui.activeMonth}.json`;
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(url);
    setNotice("已导出当前本地账本。", "success");
    renderNotice();
    renderMobileToolbox();
}

function exportCsv() {
    const header = ["日期", "类型", "分类", "金额", "备注"];
    const rows = sortBills(state.data.bills).map((bill) => [
        bill.billDate,
        BILL_TYPE_LABELS[bill.type],
        bill.categoryName,
        String(bill.amount),
        (bill.note || "").replaceAll("\n", " "),
    ]);
    const csvContent = [header, ...rows]
        .map((row) => row.map(escapeCsvCell).join(","))
        .join("\r\n");
    const blob = new Blob(["\uFEFF" + csvContent], { type: "text/csv;charset=utf-8;" });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = `bookkeeping-${state.ui.activeMonth}.csv`;
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(url);
    setNotice("已导出 CSV。", "success");
    renderNotice();
    renderMobileToolbox();
}

function importData(file) {
    if (usingFirebase()) {
        setNotice("云端同步模式下暂不支持直接导入 JSON。", "warning");
        render();
        return;
    }

    const reader = new FileReader();
    reader.onload = () => {
        try {
            const parsed = JSON.parse(String(reader.result || "{}"));
            state.data = normalizeData(parsed);
            persist();
            ensureFormDefaults();
            setNotice("导入成功，已替换当前浏览器中的本地账本。", "success");
            render();
        } catch (error) {
            setNotice(`导入失败：${error instanceof Error ? error.message : "文件格式不正确"}`, "danger");
            render();
        }
    };
    reader.readAsText(file, "utf-8");
}

async function restoreDefaultCategories() {
    if (usingFirebase() && state.app.authUser) {
        try {
            const added = await window.BookkeepingFirebase.restoreDefaultCategories(state.app.authUser.uid);
            setNotice(added > 0 ? `已向云端补齐 ${added} 个默认分类。` : "云端默认分类已经完整。", added > 0 ? "success" : "warning");
        } catch (error) {
            setNotice(error instanceof Error ? error.message : "恢复默认分类失败。", "danger");
        }
        render();
        return;
    }

    const known = new Set(state.data.categories.map((item) => `${item.type}:${item.name}`));
    DEFAULT_CATEGORIES.forEach((category) => {
        const key = `${category.type}:${category.name}`;
        if (!known.has(key)) {
            state.data.categories.push({ ...category, id: createId("category") });
        }
    });
    persist();
    ensureFormDefaults();
    setNotice("已补齐默认分类。", "success");
    render();
}

async function processRecurringBills() {
    if (usingFirebase() && state.app.authUser) {
        try {
            return await window.BookkeepingFirebase.generateDueBills(state.app.authUser.uid, state.data.recurringBills);
        } catch (error) {
            setNotice(error instanceof Error ? error.message : "生成到期账单失败。", "danger");
            return 0;
        }
    }

    const today = getToday();
    let generated = 0;

    state.data.recurringBills = state.data.recurringBills.map((item) => {
        if (!item.isActive) return item;

        let nextDueDate = item.nextDueDate || item.startDate;
        let safety = 0;

        while (nextDueDate <= today && safety < 366) {
            state.data.bills.push({
                id: createId("bill"),
                amount: item.amount,
                type: item.type,
                categoryId: item.categoryId,
                categoryName: item.categoryName,
                categoryIcon: item.categoryIcon,
                categoryColor: item.categoryColor,
                note: item.note ? `[周期] ${item.note}` : "[周期]",
                billDate: nextDueDate,
                createdAt: new Date().toISOString(),
            });
            nextDueDate = addFrequency(nextDueDate, item.frequency);
            generated += 1;
            safety += 1;
        }

        return { ...item, nextDueDate };
    });

    if (generated > 0) {
        persist();
    }

    return generated;
}

function getMonthBills(month) {
    return sortBills(state.data.bills.filter((bill) => bill.billDate.startsWith(month)));
}

function getYearBills(year) {
    return sortBills(state.data.bills.filter((bill) => bill.billDate.startsWith(`${year}-`)));
}

function getSummary(bills) {
    return bills.reduce((accumulator, bill) => {
        accumulator.count += 1;
        if (bill.type === "INCOME") {
            accumulator.income += bill.amount;
            accumulator.incomeCount += 1;
        } else {
            accumulator.expense += bill.amount;
            accumulator.expenseCount += 1;
        }
        accumulator.balance = accumulator.income - accumulator.expense;
        return accumulator;
    }, { count: 0, income: 0, expense: 0, balance: 0, incomeCount: 0, expenseCount: 0 });
}

function getCategoryStats(bills, type) {
    const filtered = bills.filter((bill) => bill.type === type);
    const total = filtered.reduce((sum, bill) => sum + bill.amount, 0);
    if (!filtered.length || total <= 0) return [];

    const groups = new Map();
    filtered.forEach((bill) => {
        if (!groups.has(bill.categoryId)) {
            groups.set(bill.categoryId, {
                category: {
                    id: bill.categoryId,
                    name: bill.categoryName,
                    icon: bill.categoryIcon,
                    color: bill.categoryColor,
                    type: bill.type,
                },
                amount: 0,
                percentage: 0,
            });
        }
        groups.get(bill.categoryId).amount += bill.amount;
    });

    return [...groups.values()]
        .map((item) => ({
            ...item,
            amount: roundCurrency(item.amount),
            percentage: item.amount / total,
        }))
        .sort((a, b) => b.amount - a.amount);
}

function groupBillsByDate(bills) {
    const groups = new Map();
    bills.forEach((bill) => {
        if (!groups.has(bill.billDate)) groups.set(bill.billDate, []);
        groups.get(bill.billDate).push(bill);
    });

    return [...groups.entries()]
        .map(([date, items]) => ({ date, items: sortBills(items) }))
        .sort((a, b) => b.date.localeCompare(a.date))
        .slice(0, 6);
}

function getDueRecurringCount() {
    const today = getToday();
    return state.data.recurringBills.filter((item) => item.isActive && item.nextDueDate <= today).length;
}

function ensureFormDefaults() {
    let added = false;
    if (!state.data.categories.length) {
        state.data.categories = DEFAULT_CATEGORIES.map((item) => ({ ...item, id: createId("category") }));
        added = true;
    }

    if (!getCategoriesByType("EXPENSE").length || !getCategoriesByType("INCOME").length) {
        const known = new Set(state.data.categories.map((item) => `${item.type}:${item.name}`));
        DEFAULT_CATEGORIES.forEach((item) => {
            const key = `${item.type}:${item.name}`;
            if (!known.has(key)) {
                state.data.categories.push({ ...item, id: createId("category") });
                added = true;
            }
        });
    }

    if (added) {
        persist();
    }

    const expenseFirst = getFirstCategoryId("EXPENSE");
    const incomeFirst = getFirstCategoryId("INCOME");

    if (!getCategoriesByType(state.ui.billForm.type).some((item) => item.id === state.ui.billForm.categoryId)) {
        state.ui.billForm.categoryId = state.ui.billForm.type === "INCOME" ? incomeFirst : expenseFirst;
    }
    if (!getCategoriesByType(state.ui.recurringForm.type).some((item) => item.id === state.ui.recurringForm.categoryId)) {
        state.ui.recurringForm.categoryId = state.ui.recurringForm.type === "INCOME" ? incomeFirst : expenseFirst;
    }
}

function getCategoriesByType(type) {
    return state.data.categories
        .filter((item) => item.type === type)
        .sort((a, b) => Number(b.isDefault) - Number(a.isDefault) || a.name.localeCompare(b.name, "zh-CN"));
}

function getFirstCategoryId(type) {
    return getCategoriesByType(type)[0]?.id || "";
}

function persist() {
    state.data.updatedAt = new Date().toISOString();
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state.data));
}

function loadData() {
    try {
        const raw = localStorage.getItem(STORAGE_KEY);
        if (!raw) return normalizeData({});
        return normalizeData(JSON.parse(raw));
    } catch {
        return normalizeData({});
    }
}

function normalizeData(input) {
    const categories = Array.isArray(input.categories) ? input.categories : [];
    const bills = Array.isArray(input.bills) ? input.bills : [];
    const recurringBills = Array.isArray(input.recurringBills) ? input.recurringBills : [];

    const normalized = {
        version: 1,
        updatedAt: typeof input.updatedAt === "string" ? input.updatedAt : new Date().toISOString(),
        categories: categories.map((category) => ({
            id: category.id || createId("category"),
            name: String(category.name || ""),
            icon: String(category.icon || "🧾"),
            color: String(category.color || "#4A90D9"),
            type: category.type === "INCOME" ? "INCOME" : "EXPENSE",
            isDefault: Boolean(category.isDefault),
        })),
        bills: bills
            .filter((bill) => bill && bill.billDate)
            .map((bill) => ({
                id: bill.id || createId("bill"),
                amount: roundCurrency(Number(bill.amount || 0)),
                type: bill.type === "INCOME" ? "INCOME" : "EXPENSE",
                categoryId: String(bill.categoryId || ""),
                categoryName: String(bill.categoryName || "未分类"),
                categoryIcon: String(bill.categoryIcon || "🧾"),
                categoryColor: String(bill.categoryColor || "#4A90D9"),
                note: String(bill.note || ""),
                billDate: normalizeDateString(bill.billDate),
                createdAt: typeof bill.createdAt === "string" ? bill.createdAt : new Date().toISOString(),
            }))
            .filter((bill) => bill.amount > 0),
        recurringBills: recurringBills
            .filter((item) => item && (item.startDate || item.nextDueDate))
            .map((item) => ({
                id: item.id || createId("recurring"),
                amount: roundCurrency(Number(item.amount || 0)),
                type: item.type === "INCOME" ? "INCOME" : "EXPENSE",
                categoryId: String(item.categoryId || ""),
                categoryName: String(item.categoryName || "未分类"),
                categoryIcon: String(item.categoryIcon || "🧾"),
                categoryColor: String(item.categoryColor || "#4A90D9"),
                note: String(item.note || ""),
                frequency: FREQUENCY_LABELS[item.frequency] ? item.frequency : "MONTHLY",
                startDate: normalizeDateString(item.startDate || item.nextDueDate),
                nextDueDate: normalizeDateString(item.nextDueDate || item.startDate),
                isActive: item.isActive !== false,
                createdAt: typeof item.createdAt === "string" ? item.createdAt : new Date().toISOString(),
            }))
            .filter((item) => item.amount > 0),
    };

    if (!normalized.categories.length) {
        normalized.categories = DEFAULT_CATEGORIES.map((item) => ({ ...item, id: createId("category") }));
    }

    return normalized;
}

function createEmptyBillForm(type) {
    return { id: "", type, amount: "", categoryId: "", note: "", billDate: getToday(), createdAt: "" };
}

function createEmptyCategoryForm(type) {
    return {
        id: "",
        type,
        name: "",
        icon: type === "INCOME" ? "💵" : "🧾",
        color: type === "INCOME" ? "#52C41A" : "#4A90D9",
        isDefault: false,
    };
}

function createEmptyRecurringForm(type) {
    return {
        id: "",
        type,
        amount: "",
        categoryId: "",
        note: "",
        frequency: "MONTHLY",
        startDate: getToday(),
        isActive: true,
        createdAt: "",
    };
}

function setNotice(text, tone) {
    state.ui.notice = { text, tone };
}

function sortBills(items) {
    return [...items].sort((a, b) => {
        if (a.billDate === b.billDate) {
            return String(b.createdAt || "").localeCompare(String(a.createdAt || ""));
        }
        return b.billDate.localeCompare(a.billDate);
    });
}

function addFrequency(dateString, frequency) {
    const date = parseDate(dateString);
    if (frequency === "DAILY") date.setDate(date.getDate() + 1);
    else if (frequency === "WEEKLY") date.setDate(date.getDate() + 7);
    else if (frequency === "YEARLY") date.setFullYear(date.getFullYear() + 1);
    else date.setMonth(date.getMonth() + 1);
    return formatDate(date);
}

function normalizeDateString(value) {
    if (typeof value !== "string" || !value.trim()) return getToday();
    if (/^\d{4}-\d{2}-\d{2}$/.test(value)) return value;
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return getToday();
    return formatDate(date);
}

function getCurrentMonth() {
    const today = new Date();
    return `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, "0")}`;
}

function getToday() {
    return formatDate(new Date());
}

function formatDate(date) {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
}

function parseDate(dateString) {
    const [year, month, day] = dateString.split("-").map(Number);
    return new Date(year, month - 1, day);
}

function formatMonthLabel(month) {
    const [year, monthValue] = month.split("-").map(Number);
    return `${year} 年 ${monthValue} 月`;
}

function formatDateLabel(dateString) {
    return new Intl.DateTimeFormat("zh-CN", {
        month: "long",
        day: "numeric",
        weekday: "short",
    }).format(parseDate(dateString));
}

function formatDateTime(value) {
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return "刚刚";
    return new Intl.DateTimeFormat("zh-CN", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit",
    }).format(date);
}

function formatCurrency(amount) {
    return new Intl.NumberFormat("zh-CN", {
        style: "currency",
        currency: "CNY",
        maximumFractionDigits: 2,
    }).format(amount || 0);
}

function roundCurrency(amount) {
    return Math.round((amount + Number.EPSILON) * 100) / 100;
}

function createId(prefix) {
    if (window.crypto && typeof window.crypto.randomUUID === "function") {
        return `${prefix}_${window.crypto.randomUUID()}`;
    }
    return `${prefix}_${Date.now()}_${Math.random().toString(36).slice(2, 9)}`;
}

function withOpacity(color, opacity) {
    if (!/^#([0-9a-f]{6})$/i.test(color)) return color;
    const safe = color.slice(1);
    const alpha = Math.round(opacity * 255).toString(16).padStart(2, "0");
    return `#${safe}${alpha}`;
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;");
}

function escapeAttribute(value) {
    return escapeHtml(value).replaceAll("`", "&#96;");
}

function escapeCsvCell(value) {
    const text = String(value ?? "");
    if (text.includes(",") || text.includes('"') || text.includes("\n")) {
        return `"${text.replaceAll('"', '""')}"`;
    }
    return text;
}

(function attachFirebaseService(window) {
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

    const COLLECTIONS = {
        bills: "bills",
        categories: "categories",
        recurring: "recurring_bills",
    };

    let auth = null;
    let firestore = null;
    let listeners = [];

    function getRawConfig() {
        return window.BOOKKEEPING_FIREBASE_CONFIG || {};
    }

    function sanitizeConfig(config) {
        return {
            apiKey: String(config.apiKey || "").trim(),
            authDomain: String(config.authDomain || "").trim(),
            projectId: String(config.projectId || "").trim(),
            storageBucket: String(config.storageBucket || "").trim(),
            messagingSenderId: String(config.messagingSenderId || "").trim(),
            appId: String(config.appId || "").trim(),
        };
    }

    function getMissingKeys(config) {
        return Object.entries(config)
            .filter(([, value]) => !value || value.includes("REPLACE_WITH") || value.includes("YOUR_"))
            .map(([key]) => key);
    }

    function init() {
        const rawConfig = getRawConfig();
        const configured = Boolean(rawConfig.enabled);
        if (!configured) {
            return { configured: false, available: false, reason: "未启用 Firebase Web 配置，当前先用本地模式。" };
        }

        if (!window.firebase) {
            return { configured: true, available: false, reason: "Firebase SDK 未加载完成。" };
        }

        const config = sanitizeConfig(rawConfig);
        const missing = getMissingKeys(config);
        if (missing.length) {
            return { configured: true, available: false, reason: `Firebase Web 配置缺少：${missing.join("、")}` };
        }

        if (!window.firebase.apps.length) {
            window.firebase.initializeApp(config);
        }

        auth = window.firebase.auth();
        firestore = window.firebase.firestore();
        firestore.enablePersistence({ synchronizeTabs: true }).catch(function ignorePersistenceError() {});
        return { configured: true, available: true, reason: "" };
    }

    function onAuthStateChanged(callback) {
        if (!auth) return function noop() {};
        return auth.onAuthStateChanged(function handleUser(user) {
            callback(user ? { uid: user.uid, email: user.email || "" } : null);
        });
    }

    function clearSubscriptions() {
        listeners.forEach(function unsubscribe(listener) {
            try {
                listener();
            } catch (_) {}
        });
        listeners = [];
    }

    function userDoc(userId) {
        return firestore.collection("users").doc(userId);
    }

    function mapCategoryDoc(doc) {
        const data = doc.data() || {};
        return {
            id: doc.id,
            name: String(data.name || ""),
            icon: String(data.icon || "🧾"),
            color: String(data.color || "#4A90D9"),
            type: data.type === "INCOME" ? "INCOME" : "EXPENSE",
            isDefault: Boolean(data.isDefault),
        };
    }

    function mapBillDoc(doc) {
        const data = doc.data() || {};
        return {
            id: doc.id,
            amount: Number(data.amount || 0),
            type: data.type === "INCOME" ? "INCOME" : "EXPENSE",
            categoryId: String(data.categoryId || ""),
            categoryName: String(data.categoryName || "未分类"),
            categoryIcon: String(data.categoryIcon || "🧾"),
            categoryColor: String(data.categoryColor || "#4A90D9"),
            note: String(data.note || ""),
            billDate: toDateString(data.billDate),
            createdAt: toIsoString(data.createdAt),
        };
    }

    function mapRecurringDoc(doc) {
        const data = doc.data() || {};
        return {
            id: doc.id,
            amount: Number(data.amount || 0),
            type: data.type === "INCOME" ? "INCOME" : "EXPENSE",
            categoryId: String(data.categoryId || ""),
            categoryName: String(data.categoryName || "未分类"),
            categoryIcon: String(data.categoryIcon || "🧾"),
            categoryColor: String(data.categoryColor || "#4A90D9"),
            note: String(data.note || ""),
            frequency: ["DAILY", "WEEKLY", "MONTHLY", "YEARLY"].includes(data.frequency) ? data.frequency : "MONTHLY",
            startDate: toDateString(data.startDate),
            nextDueDate: toDateString(data.nextDueDate),
            isActive: data.isActive !== false,
            createdAt: toIsoString(data.createdAt),
        };
    }

    function toDateString(value) {
        const date = unwrapDate(value);
        if (!date) return formatDate(new Date());
        return formatDate(date);
    }

    function toIsoString(value) {
        const date = unwrapDate(value);
        return date ? date.toISOString() : new Date().toISOString();
    }

    function unwrapDate(value) {
        if (!value) return null;
        if (typeof value.toDate === "function") return value.toDate();
        if (value instanceof Date) return value;
        const parsed = new Date(value);
        return Number.isNaN(parsed.getTime()) ? null : parsed;
    }

    function formatDate(date) {
        return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
    }

    function parseDate(dateString) {
        const [year, month, day] = String(dateString).split("-").map(Number);
        return new Date(year, month - 1, day);
    }

    function subscribeUserData(userId, handlers) {
        clearSubscriptions();
        listeners.push(
            userDoc(userId).collection(COLLECTIONS.categories).onSnapshot(function(snapshot) {
                handlers.onCategories(snapshot.docs.map(mapCategoryDoc));
            }),
            userDoc(userId).collection(COLLECTIONS.bills).orderBy("billDate", "desc").onSnapshot(function(snapshot) {
                handlers.onBills(snapshot.docs.map(mapBillDoc));
            }),
            userDoc(userId).collection(COLLECTIONS.recurring).orderBy("nextDueDate", "asc").onSnapshot(function(snapshot) {
                handlers.onRecurring(snapshot.docs.map(mapRecurringDoc));
            })
        );
    }

    async function ensureDefaultCategories(userId) {
        const categoriesRef = userDoc(userId).collection(COLLECTIONS.categories);
        const snapshot = await categoriesRef.limit(1).get();
        if (!snapshot.empty) return;

        const batch = firestore.batch();
        DEFAULT_CATEGORIES.forEach(function(category) {
            batch.set(categoriesRef.doc(), category);
        });
        await batch.commit();
    }

    async function restoreDefaultCategories(userId) {
        const categoriesRef = userDoc(userId).collection(COLLECTIONS.categories);
        const snapshot = await categoriesRef.get();
        const existing = new Set(snapshot.docs.map(function(doc) {
            const data = doc.data() || {};
            return `${data.type || "EXPENSE"}:${data.name || ""}`;
        }));
        const batch = firestore.batch();
        let added = 0;

        DEFAULT_CATEGORIES.forEach(function(category) {
            const key = `${category.type}:${category.name}`;
            if (!existing.has(key)) {
                batch.set(categoriesRef.doc(), category);
                added += 1;
            }
        });

        if (added > 0) {
            await batch.commit();
        }

        return added;
    }

    function billToFirestorePayload(bill) {
        return {
            amount: Number(bill.amount || 0),
            type: bill.type === "INCOME" ? "INCOME" : "EXPENSE",
            categoryId: bill.categoryId,
            categoryName: bill.categoryName,
            categoryIcon: bill.categoryIcon,
            categoryColor: bill.categoryColor,
            note: bill.note || "",
            billDate: parseDate(bill.billDate),
            createdAt: unwrapDate(bill.createdAt) || new Date(),
        };
    }

    function categoryToFirestorePayload(category) {
        return {
            name: category.name,
            icon: category.icon,
            color: category.color,
            type: category.type === "INCOME" ? "INCOME" : "EXPENSE",
            isDefault: Boolean(category.isDefault),
        };
    }

    function recurringToFirestorePayload(item) {
        return {
            amount: Number(item.amount || 0),
            type: item.type === "INCOME" ? "INCOME" : "EXPENSE",
            categoryId: item.categoryId,
            categoryName: item.categoryName,
            categoryIcon: item.categoryIcon,
            categoryColor: item.categoryColor,
            note: item.note || "",
            frequency: item.frequency,
            startDate: parseDate(item.startDate),
            nextDueDate: parseDate(item.nextDueDate),
            isActive: item.isActive !== false,
            createdAt: unwrapDate(item.createdAt) || new Date(),
        };
    }

    async function saveBill(userId, bill) {
        await userDoc(userId).collection(COLLECTIONS.bills).doc(bill.id).set(billToFirestorePayload(bill));
    }

    async function saveCategory(userId, category) {
        await userDoc(userId).collection(COLLECTIONS.categories).doc(category.id).set(categoryToFirestorePayload(category));
    }

    async function saveRecurring(userId, item) {
        await userDoc(userId).collection(COLLECTIONS.recurring).doc(item.id).set(recurringToFirestorePayload(item));
    }

    async function deleteBill(userId, billId) {
        await userDoc(userId).collection(COLLECTIONS.bills).doc(billId).delete();
    }

    async function deleteCategory(userId, categoryId) {
        await userDoc(userId).collection(COLLECTIONS.categories).doc(categoryId).delete();
    }

    async function deleteRecurring(userId, recurringId) {
        await userDoc(userId).collection(COLLECTIONS.recurring).doc(recurringId).delete();
    }

    async function login(email, password) {
        try {
            await auth.signInWithEmailAndPassword(email, password);
        } catch (error) {
            throw parseAuthError(error);
        }
    }

    async function register(email, password) {
        try {
            await auth.createUserWithEmailAndPassword(email, password);
        } catch (error) {
            throw parseAuthError(error);
        }
    }

    async function logout() {
        await auth.signOut();
    }

    async function sendPasswordResetEmail(email) {
        try {
            await auth.sendPasswordResetEmail(email);
        } catch (error) {
            throw parseAuthError(error);
        }
    }

    async function toggleRecurring(userId, item) {
        await saveRecurring(userId, {
            ...item,
            isActive: !item.isActive,
        });
    }

    async function generateDueBills(userId, recurringItems) {
        const today = formatDate(new Date());
        const batch = firestore.batch();
        const billsRef = userDoc(userId).collection(COLLECTIONS.bills);
        const recurringRef = userDoc(userId).collection(COLLECTIONS.recurring);
        let generated = 0;

        recurringItems.forEach(function(item) {
            if (!item.isActive) return;
            let nextDue = item.nextDueDate || item.startDate;
            let safety = 0;
            let itemGenerated = 0;

            while (nextDue <= today && safety < 366) {
                const newBillRef = billsRef.doc();
                batch.set(newBillRef, {
                    amount: Number(item.amount || 0),
                    type: item.type === "INCOME" ? "INCOME" : "EXPENSE",
                    categoryId: item.categoryId,
                    categoryName: item.categoryName,
                    categoryIcon: item.categoryIcon,
                    categoryColor: item.categoryColor,
                    note: item.note ? `[周期] ${item.note}` : "[周期]",
                    billDate: parseDate(nextDue),
                    createdAt: new Date(),
                });
                nextDue = addFrequency(nextDue, item.frequency);
                generated += 1;
                itemGenerated += 1;
                safety += 1;
            }

            if (itemGenerated > 0) {
                batch.set(recurringRef.doc(item.id), recurringToFirestorePayload({
                    ...item,
                    nextDueDate: nextDue,
                }));
            }
        });

        if (generated > 0) {
            await batch.commit();
        }

        return generated;
    }

    function parseAuthError(error) {
        const message = String(error && error.message ? error.message : "");
        const code = String(error && error.code ? error.code : "");
        if (code === "auth/invalid-credential") return new Error("邮箱或密码不正确，或者这个账号还没有注册。");
        if (code === "auth/invalid-login-credentials") return new Error("邮箱或密码不正确。");
        if (code === "auth/user-not-found") return new Error("该邮箱还没有注册。");
        if (code === "auth/wrong-password") return new Error("密码错误。");
        if (code === "auth/operation-not-allowed") return new Error("Firebase 还没有开启邮箱/密码登录。");
        if (message.includes("badly formatted")) return new Error("邮箱格式不正确。");
        if (message.includes("password is invalid")) return new Error("密码错误。");
        if (message.includes("no user record")) return new Error("该邮箱还没有注册。");
        if (message.includes("already in use")) return new Error("该邮箱已经注册过。");
        if (message.includes("at least 6 characters")) return new Error("密码至少需要 6 位。");
        if (message.includes("network")) return new Error("网络连接失败，请稍后重试。");
        return error instanceof Error ? error : new Error("认证失败。");
    }

    function addFrequency(dateString, frequency) {
        const date = parseDate(dateString);
        if (frequency === "DAILY") date.setDate(date.getDate() + 1);
        else if (frequency === "WEEKLY") date.setDate(date.getDate() + 7);
        else if (frequency === "YEARLY") date.setFullYear(date.getFullYear() + 1);
        else date.setMonth(date.getMonth() + 1);
        return formatDate(date);
    }

    window.BookkeepingFirebase = {
        init: init,
        onAuthStateChanged: onAuthStateChanged,
        clearSubscriptions: clearSubscriptions,
        ensureDefaultCategories: ensureDefaultCategories,
        restoreDefaultCategories: restoreDefaultCategories,
        subscribeUserData: subscribeUserData,
        saveBill: saveBill,
        saveCategory: saveCategory,
        saveRecurring: saveRecurring,
        deleteBill: deleteBill,
        deleteCategory: deleteCategory,
        deleteRecurring: deleteRecurring,
        login: login,
        register: register,
        logout: logout,
        sendPasswordResetEmail: sendPasswordResetEmail,
        toggleRecurring: toggleRecurring,
        generateDueBills: generateDueBills,
    };
})(window);

// frontend/jest.setup.js

// Этот файл будет автоматически выполняться перед запуском всех тестов.
// Он создает глобальные объекты, которые существуют в реальной среде (браузер/Telegram),
// но отсутствуют в тестовой среде Node.js.

global.window = {
    Telegram: {
        WebApp: {
            MainButton: {
                showProgress: jest.fn(),
                hideProgress: jest.fn(),
                setText: jest.fn(),
                enable: jest.fn(),
            },
            showAlert: jest.fn(),
        },
    },
};
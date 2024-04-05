# Rewarding Experience
Плагин для начисления опыта по таймеру, работающий проще некуда.

## Функцонал
- Начисление опыта по таймеру (По умолчанию - 10 минут)
- AFK Timer (По умолчанию - 5 минут)
- Выдача опыта сразу - ```/rwexpad``` permission: ```rewardingexperience.use```
- Изменение количества опыта по желанию в config.yml
- Отключение команды для моментальной выдачи


#### config.yml
```yml
#количество опыта
expAmount: 10
#таймер афк (в секундах)
afkTimeInSeconds: 300
#включение команды
commandEnabled: true
#интервал выдачи опыта (в секундах)
rewardIntervalInSeconds: 600
```

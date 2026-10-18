const requested = new URLSearchParams(window.location.search).get("demo");
if (requested === "1") localStorage.setItem("booksharing-demo", "1");
if (requested === "0") localStorage.removeItem("booksharing-demo");
export const isDemo = localStorage.getItem("booksharing-demo") === "1";
export const authKey = (name) => (isDemo ? "demo-" + name : name);
export function enableDemo() {
  localStorage.setItem("booksharing-demo", "1");
  window.location.assign("/?demo=1");
}
export function exitDemo() {
  localStorage.removeItem("booksharing-demo");
  window.location.assign("/?demo=0");
}
export function switchDemoUser(username) {
  localStorage.setItem("demo-token", "demo:" + username);
  localStorage.setItem(
    "demo-user",
    JSON.stringify({
      username,
      displayName: username === "alina" ? "Алина" : "Марк",
    }),
  );
  window.location.reload();
}
export function resetDemo() {
  localStorage.removeItem("booksharing-demo-data-v1");
  switchDemoUser("alina");
}

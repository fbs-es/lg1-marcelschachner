"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
Object.defineProperty(exports, "__esModule", { value: true });
const readline = __importStar(require("readline"));
const deque_1 = require("@datastructures-js/deque");
const ENEMENEMUH = [
    'ene', 'mene', 'muh', 'und', 'raus', 'bist', 'du',
    'raus', 'bist', 'du', 'noch', 'lange', 'nicht',
    'musst', 'erst', 'sagen', 'wie', 'alt', 'du', 'bist',
];
function rotate(deque, n) {
    for (let i = 0; i < n; i++) {
        deque.pushBack(deque.popFront());
    }
}
function formatOrder(deque) {
    return deque.toArray().map(s => `${s.name}(${s.age})`).join(' -> ');
}
function playRound(deque) {
    console.log(`\nReihenfolge: ${formatOrder(deque)}`);
    console.log(`\nReimzählung (${ENEMENEMUH.length} Wörter):`);
    console.log(`  "${ENEMENEMUH.join(' ')}"`);
    rotate(deque, ENEMENEMUH.length);
    console.log(`  => Neue Reihenfolge: ${formatOrder(deque)}`);
    const candidate = deque.front();
    console.log(`\nAlterszählung: ${candidate.name} ist vorne -> Alter: ${candidate.age}`);
    for (let i = 0; i < candidate.age; i++) {
        deque.pushBack(deque.popFront());
        console.log(`  Schritt ${i + 1}/${candidate.age}: ${formatOrder(deque)}`);
    }
    console.log(`\nVorne steht: ${deque.front().name} -> raus!`);
    return deque.popFront();
}
function runGame(deque) {
    if (deque.size() < 2) {
        console.log('Nicht genug Schüler zum Spielen (mindestens 2 benötigt)!');
        return;
    }
    console.log('\n=== Spiel beginnt! ===');
    console.log(`Mitspieler: ${formatOrder(deque)}`);
    let round = 1;
    while (deque.size() > 1) {
        console.log(`\n---------- Runde ${round++} ----------`);
        const eliminated = playRound(deque);
        console.log(`\n  => ${eliminated.name} scheidet aus!`);
    }
    console.log(`\n=== Gewinner: ${deque.popFront().name}! ===`);
}
const deque = new deque_1.Deque();
const rl = readline.createInterface({ input: process.stdin, output: process.stdout });
console.log('Ene Mene Muh');
console.log('----------------------------');
console.log('Schüler eingeben: "Vorname Alter" (z.B. "Marcel 25")');
console.log('Spiel starten:    "start"\n');
rl.on('line', (line) => {
    const trimmed = line.trim();
    if (trimmed.toLowerCase() === 'start') {
        rl.close();
        return;
    }
    if (trimmed === '')
        return;
    const [name, ageStr] = trimmed.split(/\s+/);
    const age = parseInt(ageStr, 10);
    if (isNaN(age) || age <= 0) {
        console.log('  Eingabe ungültig. Bitte "Vorname Alter" eingeben (z.B. "Marcel 25")');
        return;
    }
    deque.pushBack({ name, age });
    console.log(`  ${name} (${age}) wurde hinzugefügt. [${deque.size()} Schüler gesamt]`);
});
rl.on('close', () => {
    runGame(deque);
});

import * as readline from 'readline';
import { Deque } from '@datastructures-js/deque';

interface Student {
  name: string;
  age: number;
}

const ENEMENEMUH = [
  'ene', 'mene', 'muh', 'und', 'raus', 'bist', 'du',
  'raus', 'bist', 'du', 'noch', 'lange', 'nicht',
  'musst', 'erst', 'sagen', 'wie', 'alt', 'du', 'bist',
];

function rotate(deque: Deque<Student>, n: number): void {
  for (let i = 0; i < n; i++) {
    deque.pushBack(deque.popFront()!);
  }
}

function formatOrder(deque: Deque<Student>): string {
  return deque.toArray().map(s => `${s.name}(${s.age})`).join(' -> ');
}

function playRound(deque: Deque<Student>): Student {
  console.log(`\nReihenfolge: ${formatOrder(deque)}`);
  console.log(`\nReimzählung (${ENEMENEMUH.length} Wörter):`);
  console.log(`  "${ENEMENEMUH.join(' ')}"`);

  rotate(deque, ENEMENEMUH.length);

  console.log(`  => Neue Reihenfolge: ${formatOrder(deque)}`);

  const candidate = deque.front()!;
  console.log(`\nAlterszählung: ${candidate.name} ist vorne -> Alter: ${candidate.age}`);

  for (let i = 0; i < candidate.age; i++) {
    deque.pushBack(deque.popFront()!);
    console.log(`  Schritt ${i + 1}/${candidate.age}: ${formatOrder(deque)}`);
  }

  console.log(`\nVorne steht: ${deque.front()!.name} -> raus!`);
  return deque.popFront()!;
}

function runGame(deque: Deque<Student>): void {
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

  console.log(`\n=== Gewinner: ${deque.popFront()!.name}! ===`);
}

const deque = new Deque<Student>();
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

  if (trimmed === '') return;

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

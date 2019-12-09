/* Exemplo de Programa na linguagem L */

integer n;
boolean naoTerminou = false;
const MAXITER=45;
string nome;

/* Programa Principal */
main
  write('Digite seu nome: ');
  readln(nome);
  naoTerminou=true;
  n=0;
  while (naoTerminou) begin
    writeln('Ola'' ',nome);
    n=n+1;
    naoTerminou=n<MAXITER;
  end
end
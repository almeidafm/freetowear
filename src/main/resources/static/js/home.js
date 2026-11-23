(function () {
    const result = document.getElementById('random-idea');
    const shuffle = document.getElementById('shuffle-idea');

    if (!result || !shuffle) return;

    const ideas = [
        'Transforme a frase que você mais repete em uma estampa de duas cores.',
        'Misture um objeto cotidiano com um animal e crie uma cena inesperada.',
        'Escolha uma memória marcante e traduza-a em três símbolos simples.',
        'Crie uma estampa que só faça sentido para você e mais uma pessoa.',
        'Dê um novo significado a uma placa, aviso ou frase que você vê todo dia.',
        'Use uma palavra curta, uma forma geométrica e uma cor que você nunca escolheria.'
    ];

    let currentIndex = ideas.indexOf(result.textContent.trim());
    if (currentIndex < 0) currentIndex = 0;

    shuffle.addEventListener('click', function () {
        currentIndex = (currentIndex + 1) % ideas.length;
        result.textContent = ideas[currentIndex];
    });
})();
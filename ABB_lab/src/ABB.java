import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class ABB<K, V> implements IMapeamento<K, V> {

    // atributos de medição
    private long comparacoes;
    private long inicio;
    private long termino;
    
    // atributos da árvore
    private No<K, V> raiz;
    private int tamanho;
    private Comparator<K> comparador;
    
    /**
     * Construtor da classe com comparador personalizado.
     * @param comparador o comparador a ser utilizado para organizar os elementos da árvore.  
     */
    public ABB(Comparator<K> comparador) {
        this.comparador = comparador;
        this.raiz = null;
        this.tamanho = 0;
    }
    
    /**
     * Construtor padrão (assume que as chaves implementam Comparable).
     * Resolve o erro: The constructor ABB<K, Produto>() is undefined
     */
    @SuppressWarnings("unchecked")
    public ABB() {
        this.comparador = (k1, k2) -> ((Comparable<K>) k1).compareTo(k2);
        this.raiz = null;
        this.tamanho = 0;
    }
    
    public boolean vazia() {
        return raiz == null;
    }

    @Override
    public long getComparacoes() {
        return comparacoes;
    }

    @Override
    public double getTempo() {
        return (termino - inicio) / 1_000_000.0;
    }

    // ==========================================
    // TAREFA 1: Método de Recorte
    // ==========================================
    public Lista<V> recortar(K chaveInicio, K chaveFinal) {
        Lista<V> resultado = new Lista<>();
        recortarRecursivo(this.raiz, chaveInicio, chaveFinal, resultado);
        return resultado;
    }

    private void recortarRecursivo(No<K, V> no, K inicio, K fim, Lista<V> lista) {
        if (no == null) return;

        int compInicio = comparador.compare(no.getChave(), inicio);
        int compFim = comparador.compare(no.getChave(), fim);

        // Se a chave atual for maior que o início, busca na esquerda
        if (compInicio > 0) {
            recortarRecursivo(no.getEsquerda(), inicio, fim, lista);
        }

        // Se estiver dentro do intervalo (inclusive), adiciona na lista
        if (compInicio >= 0 && compFim <= 0) {
            lista.inserir(no.getItem());
        }

        // Se a chave atual for menor que o fim, busca na direita
        if (compFim < 0) {
            recortarRecursivo(no.getDireita(), inicio, fim, lista);
        }
    }

    // ==========================================
    // OPERAÇÕES BÁSICAS DA ABB (IMapeamento)
    // ==========================================
    @Override
    public int inserir(K chave, V item) {
        raiz = inserirRecursivo(raiz, chave, item);
        return ++tamanho;
    }

    private No<K, V> inserirRecursivo(No<K, V> no, K chave, V item) {
        if (no == null) {
            return new No<>(chave, item);
        }
        int cmp = comparador.compare(chave, no.getChave());
        if (cmp < 0) {
            no.setEsquerda(inserirRecursivo(no.getEsquerda(), chave, item));
        } else if (cmp > 0) {
            no.setDireita(inserirRecursivo(no.getDireita(), chave, item));
        } else {
            throw new IllegalArgumentException("O item já foi inserido anteriormente na árvore.");
        }
        return no;
    }

    @Override
    public V pesquisar(K chave) {
        inicio = System.nanoTime();
        comparacoes = 0;
        V resultado = pesquisarRecursivo(raiz, chave);
        termino = System.nanoTime();
        return resultado;
    }

    private V pesquisarRecursivo(No<K, V> no, K chave) {
        if (no == null) return null;
        
        comparacoes++;
        int cmp = comparador.compare(chave, no.getChave());
        
        if (cmp == 0) return no.getItem();
        if (cmp < 0) return pesquisarRecursivo(no.getEsquerda(), chave);
        return pesquisarRecursivo(no.getDireita(), chave);
    }

    @Override
    public V remover(K chave) {
        V itemRemovido = pesquisar(chave); 
        if (itemRemovido == null) {
            throw new IllegalArgumentException("O item a ser removido não foi localizado na árvore!");
        }
        raiz = removerRecursivo(raiz, chave);
        tamanho--;
        return itemRemovido;
    }

    private No<K, V> removerRecursivo(No<K, V> no, K chave) {
        if (no == null) return null;

        int cmp = comparador.compare(chave, no.getChave());
        if (cmp < 0) {
            no.setEsquerda(removerRecursivo(no.getEsquerda(), chave));
        } else if (cmp > 0) {
            no.setDireita(removerRecursivo(no.getDireita(), chave));
        } else {
            // Nó com apenas um filho ou nenhum
            if (no.getEsquerda() == null) return no.getDireita();
            else if (no.getDireita() == null) return no.getEsquerda();

            // Nó com dois filhos: pega o menor da subárvore direita
            No<K, V> sucessor = encontrarMinimo(no.getDireita());
            no.setChave(sucessor.getChave());
            no.setItem(sucessor.getItem());
            no.setDireita(removerRecursivo(no.getDireita(), sucessor.getChave()));
        }
        return no;
    }

    private No<K, V> encontrarMinimo(No<K, V> no) {
        while (no.getEsquerda() != null) {
            no = no.getEsquerda();
        }
        return no;
    }

    @Override
    public int tamanho() {
        return tamanho;
    }

    @Override
    public String percorrer() {
        StringBuilder sb = new StringBuilder();
        caminhamentoEmOrdemRecursivo(raiz, sb);
        return sb.toString();
    }

    private void caminhamentoEmOrdemRecursivo(No<K, V> no, StringBuilder sb) {
        if (no != null) {
            caminhamentoEmOrdemRecursivo(no.getEsquerda(), sb);
            sb.append(no.getItem().toString()).append("\n");
            caminhamentoEmOrdemRecursivo(no.getDireita(), sb);
        }
    }
    
    public String caminhamentoEmOrdem() {
        return percorrer();
    }
}
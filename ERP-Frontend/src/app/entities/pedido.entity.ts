import { Camisa } from './camisa.entity';
import { Cliente } from './cliente.entity';

export interface PedidoDTO {
  cliente: { id: number };
  formaPagamento: string;
  itens: { camisa: { id: number }, quantidade: number }[];
}

export interface Pedido {
  id: number;
  dataPedido: string;
  cliente: Cliente;
  formaPagamento: string;
  totalPedido: number;
  itens: {
    id: number;
    camisa: Camisa;
    quantidade: number;
    precoUnitario: number;
    subtotal: number;
  }[];
}

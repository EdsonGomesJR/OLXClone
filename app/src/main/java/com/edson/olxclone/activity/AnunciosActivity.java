package com.edson.olxclone.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.edson.olxclone.R;
import com.edson.olxclone.adapter.AdapterAnuncios;
import com.edson.olxclone.helper.ConfiguracaoFirebase;
import com.edson.olxclone.model.Anuncio;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class AnunciosActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;
    private DatabaseReference anuncioPublicoRef;
    private Button buttonRegiao, buttonCategoria;
    private RecyclerView recyclerViewAnunciosPublicos;
    private AdapterAnuncios adapterAnuncios;
    private List<Anuncio> anuncioList = new ArrayList<>();
    private AlertDialog dialog;
    private String filtroEstado = "";
    private String filtroCategoria = "";
    private boolean filtrandoPorEstado = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anuncios);

        //config ini
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        //config iniciais
        anuncioPublicoRef = ConfiguracaoFirebase.getFirebase()
                .child("anuncios");

        inicializarComponentes();


        //config recyclerView
        recyclerViewAnunciosPublicos.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAnunciosPublicos.setHasFixedSize(true);
        adapterAnuncios = new AdapterAnuncios(anuncioList, this);
        recyclerViewAnunciosPublicos.setAdapter(adapterAnuncios);


        recuperarAnunciosPublicos();
    }

    public void filtrarPorEstado(View view) {

        AlertDialog.Builder dialogEstado = new AlertDialog.Builder(this);
        dialogEstado.setTitle("Selecione o Estado desejado");

        //configurar spinner
        View viewSpinner = getLayoutInflater().inflate(R.layout.dialog_spinner, null);

        //spinner de estados
        final Spinner spinnerEstado = viewSpinner.findViewById(R.id.spinnerFiltro);
        String[] estados = getResources().getStringArray(R.array.estados);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, estados);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(adapter);

        dialogEstado.setView(viewSpinner);
        dialogEstado.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                filtroEstado = spinnerEstado.getSelectedItem().toString();
                recuperarAnunciosPorEstado();
                filtrandoPorEstado = true;

            }
        });

        dialogEstado.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog dialog = dialogEstado.create();
        dialog.show();

    }

    public void filtrarPorCategoria(View view) {

        if (filtrandoPorEstado == true) {
            AlertDialog.Builder dialogEstado = new AlertDialog.Builder(this);
            dialogEstado.setTitle("Selecione a Categoria desejada");

            //configurar spinner
            View viewSpinner = getLayoutInflater().inflate(R.layout.dialog_spinner, null);

            //spinner de estados
            final Spinner spinnerCategoria = viewSpinner.findViewById(R.id.spinnerFiltro);
            String[] categorias = getResources().getStringArray(R.array.categoria);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, categorias);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCategoria.setAdapter(adapter);

            dialogEstado.setView(viewSpinner);
            dialogEstado.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    filtroCategoria = spinnerCategoria.getSelectedItem().toString();
                    recuperarAnuncioPorCategoria();

                }
            });

            dialogEstado.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            AlertDialog dialog = dialogEstado.create();
            dialog.show();

        } else {

            Toast.makeText(this, "Escolha primeiro uma região", Toast.LENGTH_SHORT).show();
        }

    }

    public void recuperarAnuncioPorCategoria() {

        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setTheme(R.style.Custom)
                .setMessage("Carregando Anúncios..")
                .setCancelable(false)
                .build();
        dialog.show();

        //config nó por categoria
        anuncioPublicoRef = ConfiguracaoFirebase.getFirebase()
                .child("anuncios")
                .child(filtroEstado)
                .child(filtroCategoria);

        anuncioPublicoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                anuncioList.clear();
                /** String categoriaNome;
                 anuncioList.clear();

                 for (DataSnapshot estados : dataSnapshot.getChildren()) {
                 for (DataSnapshot categorias : estados.getChildren()) {
                 categoriaNome = categorias.getKey();

                 if (filtroCategoria.equals(categoriaNome)) {

                 for (DataSnapshot anuncios : categorias.getChildren()) {

                 Anuncio anuncio = anuncios.getValue(Anuncio.class);
                 anuncioList.add(anuncio);
                 }
                 }
                 }
                 }

                 Collections.reverse(anuncioList);
                 adapterAnuncios.notifyDataSetChanged();*/ //modo somente para retornar as categorias

                for (DataSnapshot anuncios : dataSnapshot.getChildren()) {


                    Anuncio anuncio = anuncios.getValue(Anuncio.class);
                    anuncioList.add(anuncio);
                }

                Collections.reverse(anuncioList);
                adapterAnuncios.notifyDataSetChanged();
                dialog.dismiss();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    public void recuperarAnunciosPorEstado() {

        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setTheme(R.style.Custom)
                .setMessage("Carregando Anúncios..")
                .setCancelable(false)
                .build();
        dialog.show();

        //config nó por estado
        anuncioPublicoRef = ConfiguracaoFirebase.getFirebase()
                .child("anuncios")
                .child(filtroEstado);

        anuncioPublicoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                anuncioList.clear();

                for (DataSnapshot categorias : dataSnapshot.getChildren()) {
                    for (DataSnapshot anuncios : categorias.getChildren()) {

                        Anuncio anuncio = anuncios.getValue(Anuncio.class);
                        anuncioList.add(anuncio);


                    }

                }

                Collections.reverse(anuncioList);
                adapterAnuncios.notifyDataSetChanged();
                dialog.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void recuperarAnunciosPublicos() {

        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setTheme(R.style.Custom)
                .setMessage("Carregando Anúncios..")
                .setCancelable(false)
                .build();
        dialog.show();


        anuncioPublicoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                anuncioList.clear();
                for (DataSnapshot estados : dataSnapshot.getChildren()) {
                    for (DataSnapshot categorias : estados.getChildren()) {
                        for (DataSnapshot anuncios : categorias.getChildren()) {

                            Anuncio anuncio = anuncios.getValue(Anuncio.class);
                            anuncioList.add(anuncio);


                        }

                    }

                }
                Collections.reverse(anuncioList);
                adapterAnuncios.notifyDataSetChanged();
                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (autenticacao.getCurrentUser() == null) {
            //user deslogado
            menu.setGroupVisible(R.id.group_deslogado, true);

        } else {
            //user logado

            menu.setGroupVisible(R.id.group_logado, true);

        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_cadastrar:
                startActivity(new Intent(getApplicationContext(), CadastroActivity.class));

                break;

            case R.id.menu_sair:
                autenticacao.signOut();
                invalidateOptionsMenu();
                break;

            case R.id.menu_anuncios:

                startActivity(new Intent(getApplicationContext(), MeusAnunciosActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void inicializarComponentes() {

        recyclerViewAnunciosPublicos = findViewById(R.id.recyclerAnunciosPublicos);
        buttonRegiao = findViewById(R.id.buttonRegiao);
        buttonCategoria = findViewById(R.id.buttonCategoria);

    }
}
